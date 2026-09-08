package com.eventhub.api.service;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.model.Event;
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

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private VoteService voteService;

    private User mockUser;
    private EventDateOption mockDateOption;

    @BeforeEach
    void setUp() {

        mockUser = new User(1L, "João", "joao@email.com", "senha123", Role.GUEST);

        Event mockEvent = new Event();
        mockEvent.setId(5L);

        mockDateOption = new EventDateOption();
        mockDateOption.setId(10L);
        mockDateOption.setEvent(mockEvent);


        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void castVote_Success_WhenUserHasNotVotedYet() {

        Long dateId = 10L;
        when(dateOptionRepository.findById(dateId)).thenReturn(Optional.of(mockDateOption));


        when(voteRepository.existsByUserIdAndEventDateOptionId(mockUser.getId(), dateId)).thenReturn(false); 

        Vote mockSavedVote = new Vote();
        mockSavedVote.setId(99L);
        when(voteRepository.save(any(Vote.class))).thenReturn(mockSavedVote);
        when(voteRepository.countByEventDateOptionId(dateId)).thenReturn(5L); 


        VoteResponseDTO response = voteService.castVote(dateId);


        assertNotNull(response);
        assertEquals(99L, response.voteId());
        assertEquals(5L, response.totalVotes());


        verify(voteRepository, times(1)).save(any(Vote.class)); 
    }

    @Test
    void castVote_ThrowsException_WhenUserAlreadyVoted() {

        Long dateId = 10L;
        when(dateOptionRepository.findById(dateId)).thenReturn(Optional.of(mockDateOption));


        when(voteRepository.existsByUserIdAndEventDateOptionId(mockUser.getId(), dateId)).thenReturn(true);


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            voteService.castVote(dateId);
        });


        assertEquals("Você já votou nesta data!", exception.getMessage());


        verify(voteRepository, never()).save(any(Vote.class)); 
    }
}
