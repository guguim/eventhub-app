package com.eventhub.api.repository;

import com.eventhub.api.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {


    boolean existsByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);


    long countByEventDateOptionId(Long eventDateOptionId);


    Optional<Vote> findByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);
}
