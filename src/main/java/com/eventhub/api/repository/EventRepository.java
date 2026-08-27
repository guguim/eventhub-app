package com.eventhub.api.repository;

import com.eventhub.api.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // JpaRepository já nos dá métodos como save(), findById(), findAll(), deleteById() de graça!
}
