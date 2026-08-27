package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record EventRequestDTO(
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
    String title,

    String description,
    
    String location,

    @NotNull(message = "O ID do organizador é obrigatório")
    Long organizerId,

    @NotNull(message = "As opções de datas não podem ser nulas")
    @Size(min = 1, message = "O evento deve ter pelo menos uma opção de data para votação")
    List<LocalDateTime> dateOptions
) {}
