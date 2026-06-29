package com.puntodeapoyo.notifications;

import java.util.Collection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "app.notifications.email",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoopEmailNotificationService implements EmailNotificationService {

    @Override
    public void send(Collection<String> recipients, String subject, String body) {
    }
}
