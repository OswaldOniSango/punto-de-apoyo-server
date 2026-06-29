package com.puntodeapoyo.notifications;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailNotificationProperties.class)
public class NotificationConfiguration {
}
