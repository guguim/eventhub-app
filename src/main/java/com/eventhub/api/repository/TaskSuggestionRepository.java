package com.eventhub.api.repository;

import com.eventhub.api.model.TaskSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSuggestionRepository extends JpaRepository<TaskSuggestion, Long> {

    List<TaskSuggestion> findByEventIdOrderByCreatedAtDesc(Long eventId);

    List<TaskSuggestion> findByEventIdAndRequesterIdOrderByCreatedAtDesc(Long eventId, Long requesterId);

    void deleteByEventId(Long eventId);
}
