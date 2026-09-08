package com.eventhub.api.service;

import com.eventhub.api.dto.TaskRequestDTO;
import com.eventhub.api.dto.TaskResponseDTO;
import com.eventhub.api.dto.TaskStatusUpdateDTO;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.Task;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.TaskRepository;
import com.eventhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public TaskResponseDTO createTask(Long eventId, TaskRequestDTO request) {
        User loggedUser = getAuthenticatedUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));


        if (!event.getOrganizer().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Somente o organizador do evento pode criar tarefas.");
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setEvent(event);


        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado."));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        return mapToDTO(savedTask);
    }

    public List<TaskResponseDTO> getTasksByEvent(Long eventId) {

        return taskRepository.findByEventId(eventId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO request) {
        User loggedUser = getAuthenticatedUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));


        if (task.getAssignee() == null || !task.getAssignee().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Somente o responsável pela tarefa pode alterar o seu status.");
        }

        task.setStatus(request.status());
        Task savedTask = taskRepository.save(task);

        TaskResponseDTO responseDTO = mapToDTO(savedTask);


        messagingTemplate.convertAndSend("/topic/events/" + task.getEvent().getId() + "/tasks", responseDTO);

        return responseDTO;
    }


    private TaskResponseDTO mapToDTO(Task task) {
        Long assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
        String assigneeName = task.getAssignee() != null ? task.getAssignee().getName() : null;

        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getStatus(),
                assigneeId,
                assigneeName
        );
    }
}
