package com.eventhub.api.controller;

import com.eventhub.api.dto.TaskRequestDTO;
import com.eventhub.api.dto.TaskResponseDTO;
import com.eventhub.api.dto.TaskStatusUpdateDTO;
import com.eventhub.api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    @PostMapping("/events/{eventId}/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(
            @PathVariable Long eventId,
            @Valid @RequestBody TaskRequestDTO request) {
        return new ResponseEntity<>(taskService.createTask(eventId, request), HttpStatus.CREATED);
    }


    @GetMapping("/events/{eventId}/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(taskService.getTasksByEvent(eventId));
    }


    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateDTO request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, request));
    }
}
