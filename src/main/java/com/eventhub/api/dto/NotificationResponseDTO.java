package com.eventhub.api.dto;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
    Long id,
    String message,
    boolean isRead,
    LocalDateTime createdAt
) {}
