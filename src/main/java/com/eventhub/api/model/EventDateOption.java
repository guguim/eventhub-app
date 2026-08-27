package com.eventhub.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_date_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDateOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O lado "Muitos" costuma ser o dono do relacionamento no banco, ou seja, tem a chave estrangeira (event_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDateTime dateTime;
}
