package com.example.msa.posrelay.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * storeId별 WebSocket 세션을 관리한다.
 * 현재는 전체 브로드캐스트에 getAllSessions()를 사용하며,
 * storeId 기반 라우팅이 필요해지면 getSessionsByStoreId()를 활용할 수 있다.
 */
@Component
public class PosSessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(String storeId, WebSocketSession session) {
        sessions.computeIfAbsent(storeId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void deregister(String storeId, WebSocketSession session) {
        sessions.computeIfPresent(storeId, (k, v) -> {
            v.remove(session);
            return v.isEmpty() ? null : v;
        });
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
    }

    public Set<WebSocketSession> getSessionsByStoreId(String storeId) {
        return sessions.getOrDefault(storeId, Collections.emptySet());
    }
}
