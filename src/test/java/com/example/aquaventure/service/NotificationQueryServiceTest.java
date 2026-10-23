package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.aquaventure.entity.Notification;
import com.example.aquaventure.entity.NotificationType;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.repository.NotificationRepository;

class NotificationQueryServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationQueryService service = new NotificationQueryService(notificationRepository);

    private Notification notificationFor(Long recipientId) {
        User recipient = new User();
        recipient.setId(recipientId);
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRecipient(recipient);
        notification.setType(NotificationType.BOOKING_CREATED);
        notification.setMessage("test");
        return notification;
    }

    @Test
    void markRead_ownNotification_succeeds() {
        Notification notification = notificationFor(5L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.markRead(5L, 1L);

        assertThat(dto.isRead()).isTrue();
    }

    @Test
    void markRead_someoneElsesNotification_throwsOwnershipViolation() {
        Notification notification = notificationFor(5L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markRead(999L, 1L)).isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void markAllRead_onlyTouchesCallersUnreadNotifications() {
        Notification n1 = notificationFor(5L);
        Notification n2 = notificationFor(5L);
        when(notificationRepository.findByRecipientIdAndReadFalse(5L)).thenReturn(List.of(n1, n2));

        service.markAllRead(5L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
    }
}
