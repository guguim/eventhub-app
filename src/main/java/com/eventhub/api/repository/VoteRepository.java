package com.eventhub.api.repository;

import com.eventhub.api.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {


    boolean existsByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);


    long countByEventDateOptionId(Long eventDateOptionId);


    Optional<Vote> findByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);

    @Query("SELECT v.eventDateOption.id, COUNT(v) FROM Vote v WHERE v.eventDateOption.id IN :optionIds GROUP BY v.eventDateOption.id")
    List<Object[]> countVotesByOptionIds(@Param("optionIds") List<Long> optionIds);

    @Query("SELECT v.eventDateOption.id FROM Vote v WHERE v.user.id = :userId AND v.eventDateOption.id IN :optionIds")
    List<Long> findVotedOptionIdsByUserIdAndOptionIds(@Param("userId") Long userId, @Param("optionIds") List<Long> optionIds);
}
