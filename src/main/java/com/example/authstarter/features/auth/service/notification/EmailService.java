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

import static com.example.authstarter.features.auth.constants.AsyncConstants.EMAIL_EXECUTOR;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.application.name}")
    private String appName;

    @Async(EMAIL_EXECUTOR)
    public void sendVerificationEmail(User user, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("link", link);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("verification-email", context);

        if (user != null) {
            sendHtmlEmail(user.getEmail(), "Verify your email address", htmlContent);
        }
    }

    @Async(EMAIL_EXECUTOR)
    public void resendVerificationEmail(User user, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("link", link);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("verification-resend-email", context);

        if (user != null) {
            sendHtmlEmail(user.getEmail(), "Your new verification link", htmlContent);
        }
    }

    @Async(EMAIL_EXECUTOR)
    public void sendPasswordResetEmail(User user, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("link", link);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("password-reset-email", context);

        if (user != null) {
            sendHtmlEmail(user.getEmail(), "Reset your password", htmlContent);
        }
    }

    @Async(EMAIL_EXECUTOR)
    public void sendSocialLoginReminder(User user, String provider) {
        String formattedProvider = provider.substring(0, 1).toUpperCase() +
                provider.substring(1).toLowerCase();

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("provider", formattedProvider);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("social-login-reminder-email", context);

        String subject = "Information regarding your password reset request";

        if (user != null) {
            sendHtmlEmail(user.getEmail(), subject, htmlContent);
        }
    }

    @Async(EMAIL_EXECUTOR)
    public void sendAccountDeletionCode(User user, String code) {
        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("code", code);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("account-deletion-email", context);

        String subject = "Your account deletion code";
        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    @Async(EMAIL_EXECUTOR)
    public void sendEmailChangeConfirmation(String newEmail, String token) {
        String confirmationUrl = frontendUrl + "/confirm-email?token=" + token;

        Context context = new Context();
        context.setVariable("newEmail", newEmail);
        context.setVariable("confirmationUrl", confirmationUrl);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("appName", appName);

        String htmlContent = templateEngine.process("email-change-confirmation", context);

        String subject = "Confirm your new email address";
        sendHtmlEmail(newEmail, subject, htmlContent);
    }

    private String getGreetingName(User user) {
        if (user == null || isUnset(user.getFirstName())) {
            return "there";
        }
        String name = user.getFirstName().trim();
        if (!isUnset(user.getLastName())) {
            name += " " + user.getLastName().trim();
        }
        return name;
    }

    private boolean isUnset(String value) {
        return value == null || value.isBlank() || value.equals("not-set");
    }

    private void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            throw new MessageException("Failed to send email", e);
        }
    }
}
