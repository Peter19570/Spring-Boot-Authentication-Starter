package com.example.authstarter.features.auth.service.notification;

import com.example.authstarter.features.shared.exceptions.custom.MessageException;
import com.example.authstarter.features.user.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.Year;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.authstarter.features.shared.constants.AsyncConstants.EMAIL_EXECUTOR;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.frontend-url}")
    private String frontendUrl;

    // Optional: set app.mail.from when your SMTP username isn't a real sender address (e.g. SendGrid's "apikey")
    @Value("${app.mail.from:${spring.mail.username}}")
    private String from;

    @Value("${spring.application.name}")
    private String appName;

    @Async(EMAIL_EXECUTOR)
    public void sendVerificationEmail(User user, String token) {
        send(user.getEmail(), "Verify your email address", "verification-email", Map.of(
                "greetingName", greetingName(user),
                "link", frontendUrl + "/verify-email?token=" + token));
    }

    @Async(EMAIL_EXECUTOR)
    public void resendVerificationEmail(User user, String token) {
        send(user.getEmail(), "Your new verification link", "verification-resend-email", Map.of(
                "greetingName", greetingName(user),
                "link", frontendUrl + "/verify-email?token=" + token));
    }

    @Async(EMAIL_EXECUTOR)
    public void sendPasswordResetEmail(User user, String token) {
        send(user.getEmail(), "Reset your password", "password-reset-email", Map.of(
                "greetingName", greetingName(user),
                "link", frontendUrl + "/reset-password?token=" + token));
    }

    @Async(EMAIL_EXECUTOR)
    public void sendSocialLoginReminder(User user, String provider) {
        send(user.getEmail(), "Information regarding your password reset request",
                "social-login-reminder-email", Map.of(
                        "greetingName", greetingName(user),
                        "provider", formatProvider(provider)));
    }

    @Async(EMAIL_EXECUTOR)
    public void sendAccountDeletionCode(User user, String code) {
        send(user.getEmail(), "Your account deletion code", "account-deletion-email", Map.of(
                "greetingName", greetingName(user),
                "code", code));
    }

    @Async(EMAIL_EXECUTOR)
    public void sendEmailChangeConfirmation(String newEmail, String token) {
        send(newEmail, "Confirm your new email address", "email-change-confirmation", Map.of(
                "newEmail", newEmail,
                "confirmationUrl", frontendUrl + "/confirm-email?token=" + token));
    }

    private String greetingName(User user) {
        return Objects.requireNonNullElse(user.displayName(), "there");
    }

    private String formatProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "a social account";
        }
        return Arrays.stream(provider.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" / "));
    }

    private void send(String to, String subject, String template, Map<String, ?> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process(template, context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            throw new MessageException("Failed to send email", e);
        }
    }
}