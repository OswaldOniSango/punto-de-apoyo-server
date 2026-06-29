package com.puntodeapoyo.notifications.impl;

import java.util.Collection;
import java.util.List;

import com.puntodeapoyo.notifications.EmailNotificationProperties;
import com.puntodeapoyo.notifications.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailNotificationProperties properties;

    public EmailNotificationServiceImpl(JavaMailSender mailSender, EmailNotificationProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(Collection<String> recipients, String subject, String body) {
        List<String> normalizedRecipients = recipients.stream()
                .filter(recipient -> recipient != null && !recipient.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedRecipients.isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(normalizedRecipients.toArray(String[]::new));
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.error("No se pudo enviar correo de notificacion a {}", normalizedRecipients, exception);
        }
    }
}
