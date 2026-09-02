package com.eventhub.api.controller;

import com.eventhub.api.dto.VoteResponseDTO;
import com.eventhub.api.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dates")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // Rota: POST /api/dates/{id}/vote
    // Como nós protegemos tudo no SecurityConfig, essa rota JÁ ESTÁ SEGURA!
    // Se alguém sem o JWT tentar acessar, o Spring vai dar um "Pé na porta" (Erro 401)
    // antes mesmo do código abaixo ser executado.
    @PostMapping("/{dateId}/vote")
    public ResponseEntity<VoteResponseDTO> voteForDate(@PathVariable Long dateId) {
        VoteResponseDTO response = voteService.castVote(dateId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
