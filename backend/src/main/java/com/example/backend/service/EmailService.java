package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendVerificationEmail(String recipientEmail, String userName, String token) {
        try {
            String verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token="
                    + token;

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationUrl", verificationUrl);

            String htmlContent = templateEngine.process("email-verification", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(recipientEmail);
            helper.setSubject("Vui lòng xác thực tài khoản của bạn.");
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại.", e);
        }
    }
}
