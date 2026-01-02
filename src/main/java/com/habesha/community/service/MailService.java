package com.habesha.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset your password - Habesha Community");
            
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            String emailBody = String.format(
                "Hello,\n\n" +
                "You have requested to reset your password for your Habesha Community account.\n\n" +
                "Please click the link below to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 30 minutes for security reasons.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Habesha Community Team",
                resetLink
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (MailException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendTestEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Test Email - Habesha Community");
            message.setText("This is a test email from Habesha Community backend. Mail configuration is working correctly!");
            
            mailSender.send(message);
            log.info("Test email sent successfully to: {}", toEmail);
            
        } catch (MailException e) {
            log.error("Failed to send test email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send test email", e);
        }
    }

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {} with subject: {}", toEmail, subject);
            
        } catch (MailException e) {
            log.error("Failed to send email to: {} with subject: {}", toEmail, subject, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}