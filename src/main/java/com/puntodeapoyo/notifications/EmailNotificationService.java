package com.puntodeapoyo.notifications;

import java.util.Collection;

public interface EmailNotificationService {

    void send(Collection<String> recipients, String subject, String body);
}
