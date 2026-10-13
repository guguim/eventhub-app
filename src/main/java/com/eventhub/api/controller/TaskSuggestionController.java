package com.eventhub.api.controller;

import com.eventhub.api.dto.TaskResponseDTO;
import com.eventhub.api.dto.TaskSuggestionRequestDTO;
import com.eventhub.api.dto.TaskSuggestionResponseDTO;
import com.eventhub.api.model.User;
import com.eventhub.api.service.TaskSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskSuggestionController {

    private final TaskSuggestionService suggestionService;

    @PostMapping("/events/{eventId}/task-suggestions")
    public ResponseEntity<TaskSuggestionResponseDTO> create(
            @PathVariable Long eventId,
            @Valid @RequestBody TaskSuggestionRequestDTO request,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(suggestionService.createSuggestion(eventId, request, user), HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}/task-suggestions")
    public ResponseEntity<List<TaskSuggestionResponseDTO>> list(
            @PathVariable Long eventId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestionService.getSuggestions(eventId, user));
    }

    @PostMapping("/task-suggestions/{id}/approve")
    public ResponseEntity<TaskResponseDTO> approve(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestionService.approve(id, user));
    }

    @PostMapping("/task-suggestions/{id}/reject")
    public ResponseEntity<TaskSuggestionResponseDTO> reject(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestionService.reject(id, user));
    }

    @DeleteMapping("/task-suggestions/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal User user) {
        suggestionService.cancel(id, user);
        return ResponseEntity.noContent().build();
    }
}
