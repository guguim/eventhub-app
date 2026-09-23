package com.eventhub.api.service;

import com.eventhub.api.dto.NotificationResponseDTO;
import com.eventhub.api.exception.ForbiddenAccessException;
import com.eventhub.api.exception.ResourceNotFoundException;
import com.eventhub.api.model.Notification;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;




    @Transactional
    public void createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotifications(List<User> users, String message) {
        List<Notification> notifications = users.stream().map(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            return notification;
        }).collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }


    public List<NotificationResponseDTO> getMyNotifications(User loggedUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(loggedUser.getId())
                .stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }


    public long getUnreadCount(User loggedUser) {
        return notificationRepository.countByUserIdAndIsReadFalse(loggedUser.getId());
    }


    @Transactional
    public void markAsRead(Long notificationId, User loggedUser) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada."));


        if (!notification.getUser().getId().equals(loggedUser.getId())) {
            throw new ForbiddenAccessException("Acesso negado: Esta notificação não pertence a você.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
