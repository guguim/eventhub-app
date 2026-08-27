package com.eventhub.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // "user" é uma palavra reservada no PostgreSQL, então usamos "users"
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // O banco de dados gera o ID automaticamente (auto-incremento)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // O e-mail não pode ser nulo e não pode repetir no banco
    private String email;

    @Column(nullable = false)
    private String password;
}
