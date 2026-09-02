package com.eventhub.api.repository;

import com.eventhub.api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Método mágico do Spring Data para buscar todas as tarefas de um único evento
    List<Task> findByEventId(Long eventId);
}
