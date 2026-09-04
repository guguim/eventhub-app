package com.eventhub.api.repository;

import com.eventhub.api.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Traz todas as notificações de um usuário, ordenadas da mais recente para a mais antiga!
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Conta quantas bolinhas vermelhas colocar no ícone do sininho (notificações não lidas)
    long countByUserIdAndIsReadFalse(Long userId);
}
