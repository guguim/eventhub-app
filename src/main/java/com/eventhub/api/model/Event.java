package com.eventhub.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String location;

    // Relacionamento Muitos-para-Um: Muitos eventos podem ter sido criados (organizados) por um mesmo usuário (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // Relacionamento Um-para-Muitos: Um evento pode ter várias opções de datas para votação
    // CascadeType.ALL e orphanRemoval=true garantem que se o evento for apagado, as opções de data também serão
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventDateOption> dateOptions = new ArrayList<>();
}
