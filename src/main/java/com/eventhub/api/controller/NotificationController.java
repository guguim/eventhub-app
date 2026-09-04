package com.eventhub.api.controller;

import com.eventhub.api.dto.NotificationResponseDTO;
import com.eventhub.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Retorna a lista completa para montar a interface do dropdown do sininho
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    // Rota pequenininha e leve, só para buscar o número de bolinhas vermelhas
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // PATCH: atualiza apenas o estado de "isRead" para true
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build(); // Retorna Status 204 No Content (Deu certo, mas não tenho JSON pra devolver)
    }
}
