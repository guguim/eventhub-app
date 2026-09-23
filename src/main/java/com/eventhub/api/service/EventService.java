package com.eventhub.api.service;

import com.eventhub.api.dto.EventDateOptionDTO;
import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.exception.ForbiddenAccessException;
import com.eventhub.api.exception.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResourceNotFoundException("Organizador não encontrado"));


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


        List<User> usersToNotify = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(organizer.getId()))
                .toList();

        if (!usersToNotify.isEmpty()) {
            String message = "Novo evento criado: " + event.getTitle() + " por " + organizer.getName();
            notificationService.createNotifications(usersToNotify, message);

            for (User user : usersToNotify) {
                emailService.sendSimpleEmail(
                        user.getEmail(),
                        "Convite: " + event.getTitle(),
                        "Olá " + user.getName() + ",\n\nUm novo evento foi criado!\nDetalhes: " + event.getDescription()
                );
            }
        }


        return convertToResponseDTO(savedEvent, organizer.getId());
    }

    @Transactional(readOnly = true) 
    public List<EventResponseDTO> getAllEvents(Long userId) {
        List<Event> events = eventRepository.findAll();
        List<Long> allOptionIds = events.stream()
                .flatMap(e -> e.getDateOptions().stream())
                .map(EventDateOption::getId)
                .toList();

        Map<Long, Long> voteCounts = getVoteCounts(allOptionIds);
        Set<Long> userVotedOptions = getUserVotedOptions(userId, allOptionIds);

        return events.stream()
                .map(event -> convertToResponseDTO(event, userId, voteCounts, userVotedOptions)) 
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
        return convertToResponseDTO(event, userId);
    }

    @Transactional
    public EventResponseDTO updateEvent(Long id, EventRequestDTO requestDTO, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenAccessException("Apenas o organizador pode editar este evento.");
        }

        event.setTitle(requestDTO.title());
        event.setDescription(requestDTO.description());
        event.setLocation(requestDTO.location());

        Event savedEvent = eventRepository.save(event);
        return convertToResponseDTO(savedEvent, userId);
    }

    @Transactional
    public void deleteEvent(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new ForbiddenAccessException("Apenas o organizador pode excluir este evento.");
        }

        eventRepository.delete(event);
    }


    private Map<Long, Long> getVoteCounts(List<Long> optionIds) {
        if (optionIds.isEmpty()) return Collections.emptyMap();
        return voteRepository.countVotesByOptionIds(optionIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    private Set<Long> getUserVotedOptions(Long userId, List<Long> optionIds) {
        if (userId == null || optionIds.isEmpty()) return Collections.emptySet();
        return new HashSet<>(voteRepository.findVotedOptionIdsByUserIdAndOptionIds(userId, optionIds));
    }

    private EventResponseDTO convertToResponseDTO(Event event, Long currentUserId) {
        List<Long> optionIds = event.getDateOptions().stream().map(EventDateOption::getId).toList();
        Map<Long, Long> voteCounts = getVoteCounts(optionIds);
        Set<Long> userVotedOptions = getUserVotedOptions(currentUserId, optionIds);
        return convertToResponseDTO(event, currentUserId, voteCounts, userVotedOptions);
    }

    private EventResponseDTO convertToResponseDTO(Event event, Long currentUserId, Map<Long, Long> voteCounts, Set<Long> userVotedOptions) {
        List<EventDateOptionDTO> dateOptionDTOs = event.getDateOptions().stream()
                .map(opt -> new EventDateOptionDTO(
                        opt.getId(), 
                        opt.getDateTime(),
                        voteCounts.getOrDefault(opt.getId(), 0L),
                        userVotedOptions.contains(opt.getId())
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
