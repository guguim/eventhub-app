package com.eventhub.api.controller;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.eventhub.api.model.User;

@RestController
@RequestMapping("/api/dates")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;


    @PostMapping("/{dateId}/vote")
    public ResponseEntity<VoteResponseDTO> voteForDate(@PathVariable Long dateId, @AuthenticationPrincipal User user) {
        VoteResponseDTO response = voteService.castVote(dateId, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
