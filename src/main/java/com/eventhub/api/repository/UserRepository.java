package com.eventhub.api.repository;

import com.eventhub.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // O Spring Data JPA cria a query automaticamente só pelo nome do método!
    // Equivalent a: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}
