package com.eventhub.api.service;

import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) ativa o uso do Mockito (Mock, InjectMocks) no JUnit 5
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    // O @Mock cria uma "mentira" (simulação) dos repositórios. 
    // Em testes unitários não conectamos no banco de dados real.
    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    // O @InjectMocks vai pegar os mocks acima e injetar dentro do EventService, igual o Spring faz!
    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Deve criar um evento com sucesso quando o organizador existe")
    void createEvent_Success() {
        // 1. Arrange (Preparar os dados e os Mocks)
        Long organizerId = 1L;
        User mockUser = new User(organizerId, "João", "joao@email.com", "senha123");
        
        EventRequestDTO requestDTO = new EventRequestDTO(
                "Churrasco", "Churras de fim de ano", "Casa do João", organizerId, List.of(LocalDateTime.now())
        );

        Event mockSavedEvent = new Event(10L, "Churrasco", "Churras de fim de ano", "Casa do João", mockUser, new ArrayList<>());

        // Dizendo ao Mockito como reagir: "Quando o findById for chamado com o ID 1, retorne o mockUser"
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mockUser));
        when(eventRepository.save(any(Event.class))).thenReturn(mockSavedEvent);

        // 2. Act (Executar o método que queremos testar)
        EventResponseDTO response = eventService.createEvent(requestDTO);

        // 3. Assert (Verificar se o resultado foi o esperado)
        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Churrasco", response.title());
        
        // Verificar se os repositórios foram realmente chamados
        verify(userRepository, times(1)).findById(organizerId);
        verify(eventRepository, times(1)).save(any(Event.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar criar evento com organizador inexistente")
    void createEvent_OrganizerNotFound() {
        // 1. Arrange
        EventRequestDTO requestDTO = new EventRequestDTO(
                "Churrasco", "Churras de fim de ano", "Casa do João", 999L, List.of(LocalDateTime.now())
        );
        
        // Simulando que o usuário 999 NÃO existe no banco
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert (Juntos: verificamos se a execução lança a exceção esperada)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            eventService.createEvent(requestDTO);
        });

        assertEquals("Organizador não encontrado", exception.getMessage());
        
        // Garante que a aplicação NUNCA tentou salvar o evento no banco se o usuário não existia
        verify(eventRepository, never()).save(any(Event.class));
    }
}
