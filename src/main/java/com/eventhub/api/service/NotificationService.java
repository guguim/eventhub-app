package com.eventhub.api.service;

import com.eventhub.api.dto.NotificationResponseDTO;
import com.eventhub.api.model.Notification;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    public void createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }


    public List<NotificationResponseDTO> getMyNotifications() {
        User loggedUser = getAuthenticatedUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(loggedUser.getId())
                .stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }


    public long getUnreadCount() {
        User loggedUser = getAuthenticatedUser();
        return notificationRepository.countByUserIdAndIsReadFalse(loggedUser.getId());
    }


    public void markAsRead(Long notificationId) {
        User loggedUser = getAuthenticatedUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada."));


        if (!notification.getUser().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta notificação não pertence a você.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
