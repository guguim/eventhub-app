package com.eventhub.api.service;

import com.eventhub.api.dto.TaskRequestDTO;
import com.eventhub.api.dto.TaskStatusUpdateDTO;
import com.eventhub.api.model.*;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.TaskRepository;
import com.eventhub.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TaskService taskService;

    private User loggedUser;
    private User otherUser;
    private Event mockEvent;
    private Task mockTask;

    @BeforeEach
    void setUp() {

        loggedUser = new User(1L, "Usuário Logado", "logado@email.com", "senha", Role.GUEST);


        otherUser = new User(2L, "Outro Usuário", "outro@email.com", "senha", Role.GUEST);


        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loggedUser, null, loggedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        mockEvent = new Event();
        mockEvent.setId(10L);

        mockTask = new Task();
        mockTask.setId(100L);
        mockTask.setEvent(mockEvent); 
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_ThrowsException_WhenLoggedUserIsNotTheOrganizer() {

        mockEvent.setOrganizer(otherUser);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(mockEvent));

        TaskRequestDTO request = new TaskRequestDTO("Comprar bolo", null, null, null);


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(10L, request);
        });


        assertEquals("Acesso negado: Somente o organizador do evento pode criar tarefas.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_ThrowsException_WhenLoggedUserIsNotTheAssignee() {

        mockTask.setAssignee(otherUser);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskStatusUpdateDTO request = new TaskStatusUpdateDTO(TaskStatus.COMPLETED);


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTaskStatus(100L, request);
        });


        assertEquals("Acesso negado: Somente o responsável pela tarefa pode alterar o seu status.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }
}
