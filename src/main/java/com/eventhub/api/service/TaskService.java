package com.eventhub.api.service;

import com.eventhub.api.dto.TaskRequestDTO;
import com.eventhub.api.dto.TaskResponseDTO;
import com.eventhub.api.dto.TaskStatusUpdateDTO;
import com.eventhub.api.exception.BusinessRuleException;
import com.eventhub.api.exception.ForbiddenAccessException;
import com.eventhub.api.exception.ResourceNotFoundException;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.Task;
import com.eventhub.api.model.TaskStatus;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.TaskRepository;
import com.eventhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;




    @Transactional
    public TaskResponseDTO createTask(Long eventId, TaskRequestDTO request, User loggedUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado."));


        if (!event.getOrganizer().getId().equals(loggedUser.getId())) {
            throw new ForbiddenAccessException("Acesso negado: Somente o organizador do evento pode criar tarefas.");
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setEvent(event);


        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));
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

    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO request, User loggedUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));


        if (task.getAssignee() == null || !task.getAssignee().getId().equals(loggedUser.getId())) {
            throw new ForbiddenAccessException("Acesso negado: Somente o responsável pela tarefa pode alterar o seu status.");
        }

        task.setStatus(request.status());
        Task savedTask = taskRepository.save(task);

        TaskResponseDTO responseDTO = mapToDTO(savedTask);


        messagingTemplate.convertAndSend("/topic/events/" + task.getEvent().getId() + "/tasks", responseDTO);

        return responseDTO;
    }


    @Transactional
    public TaskResponseDTO assignTask(Long taskId, User loggedUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));

        if (task.getAssignee() != null) {
            throw new BusinessRuleException("Esta tarefa já possui um responsável.");
        }

        User managedUser = userRepository.getReferenceById(loggedUser.getId());
        task.setAssignee(managedUser);
        Task savedTask = taskRepository.save(task);

        TaskResponseDTO responseDTO = mapToDTO(savedTask);
        messagingTemplate.convertAndSend("/topic/events/" + task.getEvent().getId() + "/tasks", responseDTO);

        return responseDTO;
    }

    @Transactional
    public TaskResponseDTO unassignTask(Long taskId, User loggedUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));

        boolean isOrganizer = task.getEvent().getOrganizer().getId().equals(loggedUser.getId());
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(loggedUser.getId());

        if (!isOrganizer && !isAssignee) {
            throw new ForbiddenAccessException("Apenas o organizador ou o responsável podem desatribuir esta tarefa.");
        }

        task.setAssignee(null);
        task.setStatus(TaskStatus.PENDING);
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
