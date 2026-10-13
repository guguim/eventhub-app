package com.eventhub.api.dto;

import com.eventhub.api.model.SuggestionStatus;
import java.time.LocalDateTime;

public record TaskSuggestionResponseDTO(
    Long id,
    String title,
    String description,
    LocalDateTime deadline,
    SuggestionStatus status,
    Long requesterId,
    String requesterName,
    LocalDateTime createdAt
) {}
