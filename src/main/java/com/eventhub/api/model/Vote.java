package com.eventhub.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes") // Cria a tabela 'votes' no banco de dados
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos (Many) Votos podem pertencer a Um (One) Usuário.
    // LAZY significa: não puxe os dados completos do usuário do banco até que eu explicitamente peça (melhora performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Muitos (Many) Votos podem pertencer a Uma (One) Opção de Data de Evento.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_date_option_id", nullable = false)
    private EventDateOption eventDateOption;

    // Salva automaticamente a hora em que o voto foi computado
    @Column(nullable = false, updatable = false)
    private LocalDateTime votedAt = LocalDateTime.now();
}
