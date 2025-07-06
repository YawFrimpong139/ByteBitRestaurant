package org.codewithzea.notificationservice.service;

import org.codewithzea.notificationservice.dto.EmailMessage;
import org.codewithzea.notificationservice.exception.RetryableException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Retryable(
            value = RetryableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void send(EmailMessage email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), email.isHtml());

            mailSender.send(message);
            log.info("Email sent to {}", email.getTo());
        } catch (MessagingException e) {
            throw new RetryableException("Failed to send email", e);
        }
    }
}