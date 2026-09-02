package com.eventhub.api.dto;

import com.eventhub.api.model.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponseDTO(
    Long id,
    String title,
    String description,
    LocalDateTime deadline,
    TaskStatus status,
    Long assigneeId,
    String assigneeName // Útil para o frontend mostrar "Responsável: João" sem precisar fazer outra chamada
) {}
