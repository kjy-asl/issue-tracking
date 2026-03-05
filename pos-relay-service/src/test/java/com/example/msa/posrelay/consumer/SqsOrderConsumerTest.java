package com.example.msa.posrelay.consumer;

import com.example.msa.posrelay.websocket.PosWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsOrderConsumerTest {

    @Mock PosWebSocketHandler webSocketHandler;
    @InjectMocks SqsOrderConsumer consumer;

    @Test
    void receive_메시지수신시_웹소켓에_브로드캐스트() {
        String payload = "{\"orderId\":1,\"memberId\":10}";

        consumer.receive(payload);

        verify(webSocketHandler).broadcast(payload);
    }
}
