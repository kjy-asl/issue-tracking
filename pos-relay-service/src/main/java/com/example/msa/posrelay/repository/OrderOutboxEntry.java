package com.example.msa.posrelay.repository;

import java.time.LocalDateTime;

public record OrderOutboxEntry(
        String eventId,
        String payload,
        LocalDateTime createdAt
) {}
