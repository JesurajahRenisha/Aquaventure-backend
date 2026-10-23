package com.example.aquaventure.service;

import com.example.aquaventure.entity.NotificationType;
import com.example.aquaventure.entity.User;

/** Behind an interface so email/push delivery can be added later without touching call sites. */
public interface NotificationService {

    void notify(User recipient, NotificationType type, String message);
}
