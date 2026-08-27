package com.eventhub.api.dto;

import java.util.List;

public record EventResponseDTO(
    Long id,
    String title,
    String description,
    String location,
    Long organizerId,
    String organizerName,
    List<EventDateOptionDTO> dateOptions
) {}
