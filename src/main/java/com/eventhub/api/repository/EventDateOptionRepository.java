package com.eventhub.api.repository;

import com.eventhub.api.model.EventDateOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDateOptionRepository extends JpaRepository<EventDateOption, Long> {
}
