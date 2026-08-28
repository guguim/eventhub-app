package com.eventhub.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Salva no banco de dados como texto ("ORGANIZER") e não como número (0)
    @Column(nullable = false)
    private Role role;

    // --- Métodos obrigatórios da interface UserDetails do Spring Security ---

    // Este método diz pro Spring quais são os papéis do usuário. 
    // O Spring exige o prefixo "ROLE_" para funcionar direitinho com anotações de autorização
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // No Spring Security, o "username" é a chave única de login. Para nós, é o e-mail!
    @Override
    public String getUsername() {
        return this.email;
    }

    // Os métodos abaixo poderiam ter lógica de bloqueio de conta.
    // Como não teremos isso, vamos retornar "true" (conta ativa e desbloqueada) para todos.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
