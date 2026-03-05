package com.example.msa.posrelay.consumer;

import com.example.msa.posrelay.websocket.PosWebSocketHandler;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SQS에서 주문 이벤트를 수신하여 WebSocket으로 브로드캐스트한다.
 */
@Component
public class SqsOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsOrderConsumer.class);

    private final PosWebSocketHandler webSocketHandler;

    public SqsOrderConsumer(PosWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @SqsListener(queueNames = "${pos.sqs.queue-name}")
    public void receive(String message) {
        log.info("Received SQS message, broadcasting to POS sessions");
        webSocketHandler.broadcast(message);
    }
}
