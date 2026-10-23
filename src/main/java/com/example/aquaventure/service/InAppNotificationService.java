package com.example.aquaventure.service;

import org.springframework.stereotype.Service;

import com.example.aquaventure.entity.Notification;
import com.example.aquaventure.entity.NotificationType;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.NotificationRepository;

@Service
public class InAppNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;

    public InAppNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notify(User recipient, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}
