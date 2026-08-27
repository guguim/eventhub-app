package com.eventhub.api.dto;

import java.time.LocalDateTime;

public record EventDateOptionDTO(
    Long id,
    LocalDateTime dateTime
) {}
