package com.eventhub.api.dto;

import com.eventhub.api.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

// Usado especificamente na rota de PATCH (Atualização parcial)
public record TaskStatusUpdateDTO(
    @NotNull(message = "O novo status é obrigatório")
    TaskStatus status
) {}
