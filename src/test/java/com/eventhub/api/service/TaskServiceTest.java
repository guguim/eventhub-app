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

    @InjectMocks
    private TaskService taskService;

    private User loggedUser;
    private User otherUser;
    private Event mockEvent;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        // Usuário logado simulado (Crachá ativo)
        loggedUser = new User(1L, "Usuário Logado", "logado@email.com", "senha", Role.GUEST);
        
        // Outro usuário qualquer
        otherUser = new User(2L, "Outro Usuário", "outro@email.com", "senha", Role.GUEST);

        // Injeta o crachá na memória do teste
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loggedUser, null, loggedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        mockEvent = new Event();
        mockEvent.setId(10L);

        mockTask = new Task();
        mockTask.setId(100L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_ThrowsException_WhenLoggedUserIsNotTheOrganizer() {
        // 1. CUIDADO: O organizador do evento é o "otherUser", mas quem está tentando criar a tarefa é o "loggedUser"!
        mockEvent.setOrganizer(otherUser);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(mockEvent));

        TaskRequestDTO request = new TaskRequestDTO("Comprar bolo", null, null, null);

        // 2. Tenta criar e espera que estoure o erro de segurança
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(10L, request);
        });

        // 3. Verifica se a porta foi fechada com o aviso correto
        assertEquals("Acesso negado: Somente o organizador do evento pode criar tarefas.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_ThrowsException_WhenLoggedUserIsNotTheAssignee() {
        // 1. CUIDADO: O responsável pela tarefa é o "otherUser", mas quem tá tentando mudar o status é o "loggedUser"!
        mockTask.setAssignee(otherUser);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskStatusUpdateDTO request = new TaskStatusUpdateDTO(TaskStatus.COMPLETED);

        // 2. Tenta burlar o sistema
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTaskStatus(100L, request);
        });

        // 3. Garantimos que a nossa Object-Level Security funcionou perfeitamente
        assertEquals("Acesso negado: Somente o responsável pela tarefa pode alterar o seu status.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }
}
