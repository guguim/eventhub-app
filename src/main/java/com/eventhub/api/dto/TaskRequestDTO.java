package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record TaskRequestDTO(
    @NotBlank(message = "O título da tarefa é obrigatório")
    String title,
    
    String description,
    
    LocalDateTime deadline,
    
    Long assigneeId // Opcional
) {}
