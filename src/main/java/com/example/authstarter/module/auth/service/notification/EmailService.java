package com.example.authstarter.module.auth.service.notification;

import com.example.authstarter.module.auth.exceptions.AuthenticationException;
import com.example.authstarter.module.user.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async("emailExecutor")
    public void sendVerificationEmail(User user, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("link", link);
        context.setVariable("currentYear", Year.now().getValue());

        String htmlContent = templateEngine.process("verification-email", context);

        if (user != null) {
            sendHtmlEmail(user.getEmail(), "Verify your email address", htmlContent);
        }
    }

    @Async("emailExecutor")
    public void sendPasswordResetEmail(User user, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("link", link);
        context.setVariable("currentYear", Year.now().getValue());

        String htmlContent = templateEngine.process("password-reset-email", context);

        if (user != null) {
            sendHtmlEmail(user.getEmail(), "Reset your password", htmlContent);
        }
    }

    @Async("emailExecutor")
    public void sendSocialLoginReminder(User user, String provider) {
        String formattedProvider = provider.substring(0, 1).toUpperCase() +
                provider.substring(1).toLowerCase();

        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("provider", formattedProvider);
        context.setVariable("currentYear", Year.now().getValue());

        String htmlContent = templateEngine.process("social-login-reminder-email", context);

        String subject = "Information regarding your password reset request";

        if (user != null) {
            sendHtmlEmail(user.getEmail(), subject, htmlContent);
        }
    }

    @Async("emailExecutor")
    public void sendAccountDeletionCode(User user, String code) {
        Context context = new Context();
        context.setVariable("greetingName", getGreetingName(user));
        context.setVariable("code", code);
        context.setVariable("currentYear", Year.now().getValue());

        String htmlContent = templateEngine.process("account-deletion-email", context);

        String subject = "URGENT: Account Deletion Security Code";
        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    @Async("emailExecutor")
    public void sendEmailChangeConfirmation(String newEmail, String token) {
        String confirmationUrl = frontendUrl + "/confirm-email?token=" + token;

        Context context = new Context();
        context.setVariable("newEmail", newEmail);
        context.setVariable("confirmationUrl", confirmationUrl);
        context.setVariable("currentYear", Year.now().getValue());

        String htmlContent = templateEngine.process("email-change-confirmation", context);

        String subject = "Confirm your new email address";
        sendHtmlEmail(newEmail, subject, htmlContent);
    }

    private String getGreetingName(User user) {
        if (user == null || user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return "User";
        }
        String name = user.getFirstName().trim();
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            name += " " + user.getLastName().trim();
        }
        return name;
    }

    private void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new AuthenticationException("Failed to send email");
        }
    }
}
