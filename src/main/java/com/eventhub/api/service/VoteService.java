package com.eventhub.api.service;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.model.EventDateOption;
import com.eventhub.api.model.User;
import com.eventhub.api.model.Vote;
import com.eventhub.api.repository.EventDateOptionRepository;
import com.eventhub.api.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final EventDateOptionRepository dateOptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public VoteResponseDTO castVote(Long dateOptionId) {

        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        EventDateOption dateOption = dateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new RuntimeException("Opção de data não encontrada."));


        boolean alreadyVoted = voteRepository.existsByUserIdAndEventDateOptionId(authenticatedUser.getId(), dateOptionId);
        if (alreadyVoted) {
            throw new RuntimeException("Você já votou nesta data!");
        }


        Vote vote = new Vote();
        vote.setUser(authenticatedUser);
        vote.setEventDateOption(dateOption);

        Vote savedVote = voteRepository.save(vote);


        long totalVotes = voteRepository.countByEventDateOptionId(dateOptionId);
        VoteResponseDTO responseDTO = new VoteResponseDTO(savedVote.getId(), "Voto computado com sucesso!", totalVotes);


        messagingTemplate.convertAndSend("/topic/events/" + dateOption.getEvent().getId() + "/votes", responseDTO);

        return responseDTO;
    }
}
