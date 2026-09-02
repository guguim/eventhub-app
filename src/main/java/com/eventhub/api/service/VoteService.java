package com.eventhub.api.service;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.model.EventDateOption;
import com.eventhub.api.model.User;
import com.eventhub.api.model.Vote;
import com.eventhub.api.repository.EventDateOptionRepository;
import com.eventhub.api.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final EventDateOptionRepository dateOptionRepository;

    public VoteResponseDTO castVote(Long dateOptionId) {
        // 1. Quem está votando? 
        // Em vez de receber o ID do usuário no Request Body (o que permitiria fraudes, como você enviar o ID do seu amigo),
        // nós pegamos o usuário que está autenticado AGORA na memória do Spring Security, extraído direto do Token JWT criptografado!
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. A data que ele está tentando curtir realmente existe?
        EventDateOption dateOption = dateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new RuntimeException("Opção de data não encontrada."));

        // 3. Regra Anti-Fraude:
        // Usa aquele método que criamos no Repository para ver se o cara já votou aqui.
        boolean alreadyVoted = voteRepository.existsByUserIdAndEventDateOptionId(authenticatedUser.getId(), dateOptionId);
        if (alreadyVoted) {
            throw new RuntimeException("Você já votou nesta data!");
        }

        // 4. Cria e salva o voto
        Vote vote = new Vote();
        vote.setUser(authenticatedUser);
        vote.setEventDateOption(dateOption);
        
        Vote savedVote = voteRepository.save(vote);

        // 5. Conta como ficou o total e devolve a resposta
        long totalVotes = voteRepository.countByEventDateOptionId(dateOptionId);

        return new VoteResponseDTO(savedVote.getId(), "Voto computado com sucesso!", totalVotes);
    }
}
