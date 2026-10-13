package com.eventhub.api.service;

import com.eventhub.api.dto.NotificationResponseDTO;
import com.eventhub.api.exception.ForbiddenAccessException;
import com.eventhub.api.exception.ResourceNotFoundException;
import com.eventhub.api.model.Notification;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.NotificationRepository;
import com.eventhub.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;




    @Transactional
    public void createNotification(User user, String message, Long eventId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setEventId(eventId);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotifications(List<User> users, String message, Long eventId) {
        List<Notification> notifications = users.stream().map(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setEventId(eventId);
            return notification;
        }).collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }

    @Async
    @Transactional
    public void notifyNewEventAsync(Long eventId, String eventTitle, String eventDescription, Long organizerId, String organizerName) {
        int page = 0;
        int size = 100;
        Page<User> userPage;
        String message = "Novo evento criado: " + eventTitle + " por " + organizerName;
        
        do {
            userPage = userRepository.findByIdNot(organizerId, PageRequest.of(page, size));
            List<User> users = userPage.getContent();
            if (!users.isEmpty()) {
                createNotifications(users, message, eventId);
                for (User user : users) {
                    emailService.sendSimpleEmail(
                            user.getEmail(),
                            "Convite: " + eventTitle,
                            "Olá " + user.getName() + ",\n\nUm novo evento foi criado!\nDetalhes: " + eventDescription
                    );
                }
            }
            page++;
        } while (userPage.hasNext());
    }


    public List<NotificationResponseDTO> getMyNotifications(User loggedUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(loggedUser.getId())
                .stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt(), n.getEventId()))
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
