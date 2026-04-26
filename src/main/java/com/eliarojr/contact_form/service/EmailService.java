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

    public void sendEmail(String toEmail, String subject, String htmlBody) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    //EMAIL VERIFICATION (BACKEND DIRECT)
    public void sendVerificationEmail(String toEmail, String token) {

        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + token;

        String html = """
        <div style="font-family: Arial; padding: 20px;">
            <h2 style="color:#2c3e50;">Verify Your Email</h2>

            <p>Click below to verify your account:</p>

            <a href="%s"
               style="display:inline-block;
                      padding:10px 20px;
                      background:#2ecc71;
                      color:white;
                      text-decoration:none;
                      border-radius:5px;">
                Verify Email
            </a>

            <p style="margin-top:20px; color:gray;">
                This link expires in 24 hours.
            </p>
        </div>
        """.formatted(verifyLink);

        sendEmail(toEmail, "Verify Your Email", html);
    }

    //RESET PASSWORD (FRONTEND PAGE)
    public void sendPasswordResetEmail(String toEmail, String token) {

        String resetLink = "http://localhost:8080/reset-password.html?token=" + token;

        String html = """
        <div style="font-family: Arial; padding: 20px;">
            <h2>Password Reset Request</h2>

            <p>Click below to reset your password:</p>

            <a href="%s"
               style="display:inline-block;
                      padding:10px 20px;
                      background:#e74c3c;
                      color:white;
                      text-decoration:none;
                      border-radius:5px;">
                Reset Password
            </a>

            <p style="margin-top:20px; color:gray;">
                This link expires in 30 minutes.
            </p>
        </div>
        """.formatted(resetLink);

        sendEmail(toEmail, "Reset Your Password", html);
    }
}
