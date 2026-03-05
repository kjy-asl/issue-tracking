package com.example.msa.posrelay.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PosSessionRegistryTest {

    PosSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PosSessionRegistry();
    }

    @Test
    void register_후_getAllSessions_반환() {
        WebSocketSession session = mock(WebSocketSession.class);

        registry.register("store-1", session);

        assertThat(registry.getAllSessions()).containsExactly(session);
    }

    @Test
    void register_같은_storeId에_복수_세션_등록() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);

        registry.register("store-1", s1);
        registry.register("store-1", s2);

        assertThat(registry.getAllSessions()).containsExactlyInAnyOrder(s1, s2);
    }

    @Test
    void deregister_후_getAllSessions_비어있음() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.register("store-1", session);

        registry.deregister("store-1", session);

        assertThat(registry.getAllSessions()).isEmpty();
    }

    @Test
    void deregister_하나만_제거하고_나머지_유지() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.register("store-1", s1);
        registry.register("store-1", s2);

        registry.deregister("store-1", s1);

        assertThat(registry.getAllSessions()).containsExactly(s2);
    }

    @Test
    void getSessionsByStoreId_특정_storeId_세션만_반환() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.register("store-1", s1);
        registry.register("store-2", s2);

        assertThat(registry.getSessionsByStoreId("store-1")).containsExactly(s1);
        assertThat(registry.getSessionsByStoreId("store-2")).containsExactly(s2);
    }

    @Test
    void 없는_storeId_조회시_빈_집합_반환() {
        Collection<WebSocketSession> result = registry.getSessionsByStoreId("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void 여러_storeId의_getAllSessions_합산() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        WebSocketSession s3 = mock(WebSocketSession.class);
        registry.register("store-1", s1);
        registry.register("store-2", s2);
        registry.register("store-2", s3);

        assertThat(registry.getAllSessions()).containsExactlyInAnyOrder(s1, s2, s3);
    }
}
