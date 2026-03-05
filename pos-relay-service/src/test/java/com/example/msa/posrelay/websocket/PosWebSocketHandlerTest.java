package com.example.msa.posrelay.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosWebSocketHandlerTest {

    @Mock PosSessionRegistry sessionRegistry;
    @InjectMocks PosWebSocketHandler handler;

    @Test
    void 연결_수립시_storeId로_세션_등록() throws Exception {
        WebSocketSession session = sessionWithPath("/ws/pos/store-1");

        handler.afterConnectionEstablished(session);

        verify(sessionRegistry).register("store-1", session);
    }

    @Test
    void 연결_종료시_storeId로_세션_제거() throws Exception {
        WebSocketSession session = sessionWithPath("/ws/pos/store-2");

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        verify(sessionRegistry).deregister("store-2", session);
    }

    @Test
    void broadcast_열린_세션에_메시지_전송() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(sessionRegistry.getAllSessions()).thenReturn(List.of(session));

        handler.broadcast("{\"orderId\":1}");

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        assertThat(captor.getValue().getPayload()).isEqualTo("{\"orderId\":1}");
    }

    @Test
    void broadcast_닫힌_세션은_건너뜀() throws IOException {
        WebSocketSession closed = mock(WebSocketSession.class);
        when(closed.isOpen()).thenReturn(false);
        when(sessionRegistry.getAllSessions()).thenReturn(List.of(closed));

        handler.broadcast("{\"orderId\":1}");

        verify(closed, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void broadcast_세션없으면_아무것도_안함() {
        when(sessionRegistry.getAllSessions()).thenReturn(List.of());

        handler.broadcast("{\"orderId\":1}");

        // 예외 없이 정상 완료
    }

    @Test
    void broadcast_IOException_발생해도_나머지_세션에_계속_전송() throws IOException {
        WebSocketSession failing = mock(WebSocketSession.class);
        WebSocketSession healthy = mock(WebSocketSession.class);
        when(failing.isOpen()).thenReturn(true);
        when(healthy.isOpen()).thenReturn(true);
        doThrow(new IOException("connection reset")).when(failing).sendMessage(any());
        when(sessionRegistry.getAllSessions()).thenReturn(List.of(failing, healthy));

        handler.broadcast("{\"orderId\":1}"); // 예외가 외부로 전파되지 않아야 한다

        verify(healthy).sendMessage(any(TextMessage.class));
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private WebSocketSession sessionWithPath(String path) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(URI.create("ws://localhost" + path));
        when(session.getId()).thenReturn("session-" + path.hashCode());
        return session;
    }
}
