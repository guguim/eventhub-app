package com.eventhub.api.service;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.model.EventDateOption;
import com.eventhub.api.model.Role;
import com.eventhub.api.model.User;
import com.eventhub.api.model.Vote;
import com.eventhub.api.repository.EventDateOptionRepository;
import com.eventhub.api.repository.VoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private EventDateOptionRepository dateOptionRepository;

    @InjectMocks
    private VoteService voteService;

    private User mockUser;
    private EventDateOption mockDateOption;

    @BeforeEach
    void setUp() {
        // 1. Criamos os dados fictícios
        mockUser = new User(1L, "João", "joao@email.com", "senha123", Role.GUEST);
        mockDateOption = new EventDateOption();
        mockDateOption.setId(10L);

        // 2. TRUQUE MÁGICO: O nosso código roda pegando o usuário da sessão do Spring Security.
        // Como no teste unitário nós não temos um servidor web rodando e logando de verdade,
        // nós temos que "injetar" manualmente na memória do teste um "Crachá" com o nosso mockUser!
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @AfterEach
    void tearDown() {
        // Limpamos a memória de segurança ao fim de cada teste
        SecurityContextHolder.clearContext();
    }

    @Test
    void castVote_Success_WhenUserHasNotVotedYet() {
        // Arrange (Preparar)
        Long dateId = 10L;
        when(dateOptionRepository.findById(dateId)).thenReturn(Optional.of(mockDateOption));
        
        // Finge que o banco disse: "Não, ele ainda não votou"
        when(voteRepository.existsByUserIdAndEventDateOptionId(mockUser.getId(), dateId)).thenReturn(false); 
        
        Vote mockSavedVote = new Vote();
        mockSavedVote.setId(99L);
        when(voteRepository.save(any(Vote.class))).thenReturn(mockSavedVote);
        when(voteRepository.countByEventDateOptionId(dateId)).thenReturn(5L); // Total simulado de 5 votos

        // Act (Agir)
        VoteResponseDTO response = voteService.castVote(dateId);

        // Assert (Garantir)
        assertNotNull(response);
        assertEquals(99L, response.voteId());
        assertEquals(5L, response.totalVotes());
        
        // Verifica se o repositório foi chamado pra salvar exatamente 1 vez
        verify(voteRepository, times(1)).save(any(Vote.class)); 
    }

    @Test
    void castVote_ThrowsException_WhenUserAlreadyVoted() {
        // Arrange (Preparar)
        Long dateId = 10L;
        when(dateOptionRepository.findById(dateId)).thenReturn(Optional.of(mockDateOption));
        
        // AQUI ESTÁ A CHAVE: Finge que o banco disse: "Sim, ele já curtiu essa data antes!"
        when(voteRepository.existsByUserIdAndEventDateOptionId(mockUser.getId(), dateId)).thenReturn(true);

        // Act & Assert (Tenta rodar e espera que estoure um erro)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            voteService.castVote(dateId);
        });

        // Garante que o texto do erro é o correto
        assertEquals("Você já votou nesta data!", exception.getMessage());
        
        // A MAIOR GARANTIA DE SEGURANÇA: Verifica que NUNCA foi chamado o método save() no banco!
        verify(voteRepository, never()).save(any(Vote.class)); 
    }
}
