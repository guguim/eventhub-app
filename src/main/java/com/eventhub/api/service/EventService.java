package com.eventhub.api.service;

import com.eventhub.api.dto.EventDateOptionDTO;
import com.eventhub.api.dto.EventRequestDTO;
import com.eventhub.api.dto.EventResponseDTO;
import com.eventhub.api.model.Event;
import com.eventhub.api.model.EventDateOption;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.EventRepository;
import com.eventhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // Marca a classe como um componente de serviço do Spring
@RequiredArgsConstructor // Cria um construtor com os atributos "final" automaticamente, injetando as dependências!
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // @Transactional garante que ou salva TUDO ou não salva NADA (rollback se der erro no meio)
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO) {
        
        // 1. Busca o organizador. Se não achar, lança um erro.
        // (Mais tarde, na próxima etapa de Controllers, criaremos uma NotFoundException bonitinha)
        User organizer = userRepository.findById(requestDTO.organizerId())
                .orElseThrow(() -> new RuntimeException("Organizador não encontrado"));

        // 2. Transformamos o DTO (Request) em Entidade (para salvar no banco)
        Event event = new Event();
        event.setTitle(requestDTO.title());
        event.setDescription(requestDTO.description());
        event.setLocation(requestDTO.location());
        event.setOrganizer(organizer);

        // 3. Convertendo as datas recebidas para a entidade EventDateOption e VINCULANDO ao evento
        List<EventDateOption> options = requestDTO.dateOptions().stream()
                .map(dateTime -> {
                    EventDateOption option = new EventDateOption();
                    option.setDateTime(dateTime);
                    option.setEvent(event); // IMPORTANTE: Define o dono do relacionamento bidirecional
                    return option;
                }).toList();
        
        event.setDateOptions(options);

        // 4. Salva no banco. Como usamos CascadeType.ALL, salvar o "Event" salva as "EventDateOption" juntas!
        Event savedEvent = eventRepository.save(event);

        // 5. Integração com Notificações (Fase 6)
        // Busca todos os usuários do banco (menos o organizador) para avisá-los.
        // NOTA DIDÁTICA: Em produção, NUNCA faça disparos em massa de e-mail de forma "síncrona" (travando a requisição HTTP).
        // A API demoraria minutos para responder. O correto é jogar numa fila (RabbitMQ/Kafka) ou usar @Async.
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.getId().equals(organizer.getId())) {
                String message = "Novo evento criado: " + event.getTitle() + " por " + organizer.getName();
                
                // Dispara o Sininho (Salva no banco)
                notificationService.createNotification(user, message);
                
                // Dispara o Email SMTP
                emailService.sendSimpleEmail(
                        user.getEmail(), 
                        "Convite: " + event.getTitle(), 
                        "Olá " + user.getName() + ",\n\nUm novo evento foi criado!\nDetalhes: " + event.getDescription()
                );
            }
        }

        // 6. Retornamos o DTO de Response, protegendo as entidades
        return convertToResponseDTO(savedEvent);
    }

    @Transactional(readOnly = true) // readOnly = true melhora a performance de buscas no banco
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToResponseDTO) // Transforma cada Entidade num DTO
                .toList();
    }

    // Método auxiliar (privado) para não repetir código de conversão
    private EventResponseDTO convertToResponseDTO(Event event) {
        List<EventDateOptionDTO> dateOptionDTOs = event.getDateOptions().stream()
                .map(opt -> new EventDateOptionDTO(opt.getId(), opt.getDateTime()))
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
