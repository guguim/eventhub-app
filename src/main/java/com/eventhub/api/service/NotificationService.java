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

    // 1. GERAÇÃO (Uso Interno)
    // Esse método não será exposto para a Web. Será chamado pelos nossos outros Services (ex: EventService) 
    // quando eles quiserem gerar uma notificação no banco para um usuário.
    public void createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    // 2. LEITURA (Para o Frontend)
    // Pega as notificações apenas de quem estiver com o Token JWT ativo no momento da requisição.
    public List<NotificationResponseDTO> getMyNotifications() {
        User loggedUser = getAuthenticatedUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(loggedUser.getId())
                .stream()
                .map(n -> new NotificationResponseDTO(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // 3. CONTAGEM (Bolinha vermelha no sino)
    public long getUnreadCount() {
        User loggedUser = getAuthenticatedUser();
        return notificationRepository.countByUserIdAndIsReadFalse(loggedUser.getId());
    }

    // 4. ATUALIZAÇÃO (Marcar como lida)
    public void markAsRead(Long notificationId) {
        User loggedUser = getAuthenticatedUser();
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada."));

        // Object-Level Security: Um usuário não pode marcar a notificação do outro como lida.
        if (!notification.getUser().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta notificação não pertence a você.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
