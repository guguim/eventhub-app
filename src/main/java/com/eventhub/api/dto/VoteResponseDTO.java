package com.eventhub.api.dto;

public record VoteResponseDTO(
    Long voteId,
    String message,
    long totalVotes
) {}
