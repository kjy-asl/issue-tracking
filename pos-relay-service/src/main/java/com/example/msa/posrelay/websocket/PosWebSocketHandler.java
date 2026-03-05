package com.example.msa.posrelay.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * POS 시스템과 WebSocket 연결을 관리하고 주문 이벤트를 브로드캐스트한다.
 * 엔드포인트: /ws/pos/{storeId}
 */
@Component
public class PosWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PosWebSocketHandler.class);

    private final PosSessionRegistry sessionRegistry;

    public PosWebSocketHandler(PosSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String storeId = extractStoreId(session.getUri().getPath());
        sessionRegistry.register(storeId, session);
        log.info("POS connected: storeId={}, sessionId={}", storeId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String storeId = extractStoreId(session.getUri().getPath());
        sessionRegistry.deregister(storeId, session);
        log.info("POS disconnected: storeId={}, sessionId={}", storeId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // POS → 서버 방향 메시지는 현재 사용하지 않음
    }

    public void broadcast(String payload) {
        TextMessage message = new TextMessage(payload);
        sessionRegistry.getAllSessions().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message to sessionId={}", session.getId(), e);
                }
            }
        });
    }

    private String extractStoreId(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
