package com.eventhub.api.controller;

import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.service.EventService;
import com.eventhub.api.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        EventResponseDTO response = eventService.createEvent(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(@AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(eventService.getAllEvents(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(eventService.getEventById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(eventService.updateEvent(id, requestDTO, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;
        eventService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }
}
