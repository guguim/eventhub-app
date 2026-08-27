package com.eventhub.api.controller;

import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que é um Controller de API REST (já inclui @ResponseBody automaticamente)
@RequestMapping("/api/events") // Define que todas as rotas aqui começam com /api/events
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService; // Injetado via construtor do Lombok

    // Recebe POST em /api/events
    // O @Valid liga a validação das anotações que colocamos no DTO (@NotBlank, etc)
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        EventResponseDTO response = eventService.createEvent(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED); // HTTP 201 (Created)
    }

    // Recebe GET em /api/events
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents()); // HTTP 200 (OK)
    }
}
