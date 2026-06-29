package com.puntodeapoyo.notifications;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifications.email")
public record EmailNotificationProperties(
        boolean enabled,
        String from,
        List<String> internalRecipients
) {
}
