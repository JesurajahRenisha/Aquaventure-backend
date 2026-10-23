package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.notification.NotificationDto;
import com.example.aquaventure.entity.Notification;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.NotificationMapper;
import com.example.aquaventure.repository.NotificationRepository;

@Service
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> listForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientId(userId, pageable).map(NotificationMapper::toDto);
    }

    @Transactional
    public NotificationDto markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new OwnershipViolationException("This notification does not belong to you");
        }
        notification.setRead(true);
        return NotificationMapper.toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByRecipientIdAndReadFalse(userId).forEach(n -> n.setRead(true));
    }
}
