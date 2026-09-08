package com.eventhub.api.service;

import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.Role;
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


@ExtendWith(MockitoExtension.class)
class EventServiceTest {


    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private com.eventhub.api.repository.VoteRepository voteRepository;


    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Deve criar um evento com sucesso quando o organizador existe")
    void createEvent_Success() {

        Long organizerId = 1L;
        User mockUser = new User(organizerId, "João", "joao@email.com", "senha123", Role.ORGANIZER);

        EventRequestDTO requestDTO = new EventRequestDTO(
                "Churrasco", "Churras de fim de ano", "Casa do João", organizerId, List.of(LocalDateTime.now())
        );

        Event mockSavedEvent = new Event(10L, "Churrasco", "Churras de fim de ano", "Casa do João", mockUser, new ArrayList<>());


        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mockUser));
        when(eventRepository.save(any(Event.class))).thenReturn(mockSavedEvent);
        when(userRepository.findAll()).thenReturn(List.of(mockUser)); 


        EventResponseDTO response = eventService.createEvent(requestDTO);


        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Churrasco", response.title());


        verify(userRepository, times(1)).findById(organizerId);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar evento com organizador inexistente")
    void createEvent_OrganizerNotFound() {

        EventRequestDTO requestDTO = new EventRequestDTO(
                "Churrasco", "Churras de fim de ano", "Casa do João", 999L, List.of(LocalDateTime.now())
        );


        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        Exception exception = assertThrows(RuntimeException.class, () -> {
            eventService.createEvent(requestDTO);
        });

        assertEquals("Organizador não encontrado", exception.getMessage());


        verify(eventRepository, never()).save(any(Event.class));
    }
}
