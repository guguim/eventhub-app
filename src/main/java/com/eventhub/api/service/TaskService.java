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

    // Método utilitário para pegar o usuário do Token (nosso truque de segurança!)
    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public TaskResponseDTO createTask(Long eventId, TaskRequestDTO request) {
        User loggedUser = getAuthenticatedUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));

        // 1ª REGRA DE AUTORIZAÇÃO (Object-Level Security)
        // Só deixamos passar se o ID do organizador do evento for IGUAL ao ID do cara logado no Token
        if (!event.getOrganizer().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Somente o organizador do evento pode criar tarefas.");
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setEvent(event);

        // Se ele mandou um responsável no JSON, nós buscamos no banco e conectamos na Tarefa
        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado."));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        return mapToDTO(savedTask);
    }

    public List<TaskResponseDTO> getTasksByEvent(Long eventId) {
        // Busca todas do banco, converte uma por uma de Entidade para DTO e devolve a lista
        return taskRepository.findByEventId(eventId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO request) {
        User loggedUser = getAuthenticatedUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        // 2ª REGRA DE AUTORIZAÇÃO
        // Só deixamos passar se o dono da tarefa existir E for IGUAL ao cara logado no Token
        if (task.getAssignee() == null || !task.getAssignee().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Somente o responsável pela tarefa pode alterar o seu status.");
        }

        task.setStatus(request.status());
        Task savedTask = taskRepository.save(task);
        
        return mapToDTO(savedTask);
    }

    // Utilitário para traduzir a Entidade (cheia de relacionamentos pesados) 
    // para um DTO levinho só com os dados que o React precisa.
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
