package com.example.msa.posrelay.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "relay_pointer")
public class RelayPointer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_relayed_at", nullable = false)
    private LocalDateTime lastRelayedAt;

    @Column(name = "last_event_id", length = 36)
    private String lastEventId;

    protected RelayPointer() {}

    public Long getId() { return id; }
    public LocalDateTime getLastRelayedAt() { return lastRelayedAt; }
    public String getLastEventId() { return lastEventId; }

    public void update(LocalDateTime lastRelayedAt, String lastEventId) {
        this.lastRelayedAt = lastRelayedAt;
        this.lastEventId = lastEventId;
    }
}
