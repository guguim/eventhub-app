package com.eventhub.api.service;

import com.eventhub.api.dto.EventDateOptionDTO;
import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.EventDateOption;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.UserRepository;
import com.eventhub.api.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service 
@RequiredArgsConstructor 
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final VoteRepository voteRepository;


    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO) {


        User organizer = userRepository.findById(requestDTO.organizerId())
                .orElseThrow(() -> new RuntimeException("Organizador não encontrado"));


        Event event = new Event();
        event.setTitle(requestDTO.title());
        event.setDescription(requestDTO.description());
        event.setLocation(requestDTO.location());
        event.setOrganizer(organizer);


        List<EventDateOption> options = requestDTO.dateOptions().stream()
                .map(dateTime -> {
                    EventDateOption option = new EventDateOption();
                    option.setDateTime(dateTime);
                    option.setEvent(event); 
                    return option;
                }).toList();

        event.setDateOptions(options);


        Event savedEvent = eventRepository.save(event);


        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.getId().equals(organizer.getId())) {
                String message = "Novo evento criado: " + event.getTitle() + " por " + organizer.getName();


                notificationService.createNotification(user, message);


                emailService.sendSimpleEmail(
                        user.getEmail(), 
                        "Convite: " + event.getTitle(), 
                        "Olá " + user.getName() + ",\n\nUm novo evento foi criado!\nDetalhes: " + event.getDescription()
                );
            }
        }


        return convertToResponseDTO(savedEvent);
    }

    @Transactional(readOnly = true) 
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToResponseDTO) 
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        return convertToResponseDTO(event);
    }


    private EventResponseDTO convertToResponseDTO(Event event) {
        List<EventDateOptionDTO> dateOptionDTOs = event.getDateOptions().stream()
                .map(opt -> new EventDateOptionDTO(
                        opt.getId(), 
                        opt.getDateTime(),
                        voteRepository.countByEventDateOptionId(opt.getId()) 
                ))
                .toList();

        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getOrganizer().getId(),
                event.getOrganizer().getName(),
                dateOptionDTOs
        );
    }
}
