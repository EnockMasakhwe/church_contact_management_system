package com.eliarojr.contact_form.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private void sendEmail(String toEmail, String subject, String htmlBody) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendVerificationEmail(String toEmail, String token){
        String subject = "Verify your account";

        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        String message = "Click the link to verify your account:\n" + verificationUrl;

        sendEmail(toEmail, subject, message);

    }

    public void sendPasswordResetEmail(String toEmail, String token) {

        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;

        String subject = "Password Reset Request";

        String body = "Hello,\n\n"
                + "We received a request to reset your password.\n\n"
                + "Click the link below to reset your password:\n"
                + resetLink + "\n\n"
                + "This link will expire in 30 minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\n"
                + "Your Support Team";

        sendEmail(toEmail, subject, body);
    }
}
