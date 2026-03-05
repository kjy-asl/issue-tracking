package com.example.msa.posrelay.config;

import com.example.msa.posrelay.websocket.PosWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PosWebSocketHandler posWebSocketHandler;

    public WebSocketConfig(PosWebSocketHandler posWebSocketHandler) {
        this.posWebSocketHandler = posWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(posWebSocketHandler, "/ws/pos/{storeId}")
                .setAllowedOrigins("*");
    }
}
