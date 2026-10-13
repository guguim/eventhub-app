package com.eventhub.api.service;

import com.eventhub.api.dto.TaskResponseDTO;
import com.eventhub.api.dto.TaskSuggestionRequestDTO;
import com.eventhub.api.dto.TaskSuggestionResponseDTO;
import com.eventhub.api.exception.BusinessRuleException;
import com.eventhub.api.exception.ForbiddenAccessException;
import com.eventhub.api.exception.ResourceNotFoundException;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.SuggestionStatus;
import com.eventhub.api.model.Task;
import com.eventhub.api.model.TaskSuggestion;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.TaskRepository;
import com.eventhub.api.repository.TaskSuggestionRepository;
import com.eventhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluxo de "pull request" de tarefas: participantes sugerem, o organizador aprova ou rejeita.
 * Ao aprovar, a tarefa é criada no evento.
 */
@Service
@RequiredArgsConstructor
public class TaskSuggestionService {

    private final TaskSuggestionRepository suggestionRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TaskSuggestionResponseDTO createSuggestion(Long eventId, TaskSuggestionRequestDTO request, User loggedUser) {
        Event event = findEvent(eventId);

        if (isOrganizer(event, loggedUser)) {
            throw new BusinessRuleException("O organizador pode criar tarefas diretamente, sem precisar de sugestão.");
        }

        User requester = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        TaskSuggestion suggestion = new TaskSuggestion();
        suggestion.setTitle(request.title().trim());
        suggestion.setDescription(request.description());
        suggestion.setDeadline(request.deadline());
        suggestion.setEvent(event);
        suggestion.setRequester(requester);

        TaskSuggestion saved = suggestionRepository.save(suggestion);

        notificationService.createNotification(event.getOrganizer(),
                requester.getName() + " sugeriu a tarefa \"" + saved.getTitle() + "\" no evento \"" + event.getTitle() + "\".",
                eventId);
        broadcast(eventId);

        return mapToDTO(saved);
    }

    /** Organizador vê todas as sugestões; os demais veem apenas as suas. */
    @Transactional(readOnly = true)
    public List<TaskSuggestionResponseDTO> getSuggestions(Long eventId, User loggedUser) {
        Event event = findEvent(eventId);

        List<TaskSuggestion> list = isOrganizer(event, loggedUser)
                ? suggestionRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                : suggestionRepository.findByEventIdAndRequesterIdOrderByCreatedAtDesc(eventId, loggedUser.getId());

        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponseDTO approve(Long suggestionId, User loggedUser) {
        TaskSuggestion suggestion = findPendingForOrganizer(suggestionId, loggedUser,
                "Acesso negado: Somente o organizador do evento pode aprovar sugestões.");

        Task task = new Task();
        task.setTitle(suggestion.getTitle());
        task.setDescription(suggestion.getDescription());
        task.setDeadline(suggestion.getDeadline());
        task.setEvent(suggestion.getEvent());
        Task savedTask = taskRepository.save(task);

        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestion.setReviewedAt(LocalDateTime.now());
        suggestionRepository.save(suggestion);

        notificationService.createNotification(suggestion.getRequester(),
                "Sua sugestão \"" + suggestion.getTitle() + "\" foi aprovada no evento \""
                        + suggestion.getEvent().getTitle() + "\"!",
                suggestion.getEvent().getId());

        broadcast(suggestion.getEvent().getId());

        return new TaskResponseDTO(savedTask.getId(), savedTask.getTitle(), savedTask.getDescription(),
                savedTask.getDeadline(), savedTask.getStatus(), null, null);
    }

    @Transactional
    public TaskSuggestionResponseDTO reject(Long suggestionId, User loggedUser) {
        TaskSuggestion suggestion = findPendingForOrganizer(suggestionId, loggedUser,
                "Acesso negado: Somente o organizador do evento pode rejeitar sugestões.");

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setReviewedAt(LocalDateTime.now());
        TaskSuggestion saved = suggestionRepository.save(suggestion);

        notificationService.createNotification(suggestion.getRequester(),
                "Sua sugestão \"" + suggestion.getTitle() + "\" foi recusada no evento \""
                        + suggestion.getEvent().getTitle() + "\".",
                suggestion.getEvent().getId());

        broadcast(suggestion.getEvent().getId());
        return mapToDTO(saved);
    }

    /** O autor pode cancelar a própria sugestão enquanto ela estiver pendente. */
    @Transactional
    public void cancel(Long suggestionId, User loggedUser) {
        TaskSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão não encontrada."));

        if (!suggestion.getRequester().getId().equals(loggedUser.getId())) {
            throw new ForbiddenAccessException("Acesso negado: Somente o autor pode cancelar a sugestão.");
        }
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new BusinessRuleException("Esta sugestão já foi analisada.");
        }

        Long eventId = suggestion.getEvent().getId();
        suggestionRepository.delete(suggestion);
        broadcast(eventId);
    }

    private TaskSuggestion findPendingForOrganizer(Long suggestionId, User loggedUser, String forbiddenMessage) {
        TaskSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão não encontrada."));

        if (!isOrganizer(suggestion.getEvent(), loggedUser)) {
            throw new ForbiddenAccessException(forbiddenMessage);
        }
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new BusinessRuleException("Esta sugestão já foi analisada.");
        }
        return suggestion;
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado."));
    }

    private boolean isOrganizer(Event event, User user) {
        return event.getOrganizer().getId().equals(user.getId());
    }

    private void broadcast(Long eventId) {
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/suggestions", "updated");
    }

    private TaskSuggestionResponseDTO mapToDTO(TaskSuggestion s) {
        return new TaskSuggestionResponseDTO(
                s.getId(), s.getTitle(), s.getDescription(), s.getDeadline(), s.getStatus(),
                s.getRequester().getId(), s.getRequester().getName(), s.getCreatedAt());
    }
}
