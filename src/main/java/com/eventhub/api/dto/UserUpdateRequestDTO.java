package com.eventhub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDTO(
        @NotBlank(message = "O nome não pode ser vazio")
        String name,

        @NotBlank(message = "O e-mail não pode ser vazio")
        @Email(message = "Formato de e-mail inválido")
        String email
) {}
