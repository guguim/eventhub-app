package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record TaskSuggestionRequestDTO(
    @NotBlank(message = "O título da sugestão é obrigatório")
    String title,

    String description,

    LocalDateTime deadline
) {}
