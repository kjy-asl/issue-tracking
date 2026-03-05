package com.example.msa.posrelay.publisher;

import com.example.msa.posrelay.domain.RelayPointer;
import com.example.msa.posrelay.repository.OrderOutboxEntry;
import com.example.msa.posrelay.repository.OrderOutboxReadRepository;
import com.example.msa.posrelay.repository.RelayPointerRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

/**
 * order_outbox 테이블을 주기적으로 폴링하여 SQS에 발행한다.
 * Debezium이 order_outbox → Kafka를 담당하듯, 이 컴포넌트가 order_outbox → SQS를 담당한다.
 */
@Component
public class SqsOutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(SqsOutboxPoller.class);
    private static final int BATCH_SIZE = 50;

    private final RelayPointerRepository relayPointerRepository;
    private final OrderOutboxReadRepository orderOutboxReadRepository;
    private final SqsAsyncClient sqsAsyncClient;

    @Value("${pos.sqs.queue-name}")
    private String queueName;

    private String queueUrl;

    public SqsOutboxPoller(RelayPointerRepository relayPointerRepository,
                           OrderOutboxReadRepository orderOutboxReadRepository,
                           SqsAsyncClient sqsAsyncClient) {
        this.relayPointerRepository = relayPointerRepository;
        this.orderOutboxReadRepository = orderOutboxReadRepository;
        this.sqsAsyncClient = sqsAsyncClient;
    }

    @PostConstruct
    public void init() {
        for (int attempt = 1; attempt <= 10; attempt++) {
            try {
                queueUrl = sqsAsyncClient.getQueueUrl(b -> b.queueName(queueName))
                        .join()
                        .queueUrl();
                log.info("SQS queue URL resolved: {}", queueUrl);
                return;
            } catch (Exception e) {
                log.warn("SQS queue '{}' not ready yet (attempt {}/10), retrying in 3s...", queueName, attempt);
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        }
        throw new IllegalStateException("Failed to resolve SQS queue URL for: " + queueName);
    }

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        RelayPointer pointer = relayPointerRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("relay_pointer record not found (id=1)"));

        List<OrderOutboxEntry> entries = orderOutboxReadRepository.findAfter(pointer.getLastRelayedAt(), BATCH_SIZE);

        if (entries.isEmpty()) {
            return;
        }

        try {
            for (OrderOutboxEntry entry : entries) {
                sqsAsyncClient.sendMessage(SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(entry.payload())
                        .build()).join();
                log.debug("Published to SQS: eventId={}", entry.eventId());
            }

            OrderOutboxEntry last = entries.get(entries.size() - 1);
            pointer.update(last.createdAt(), last.eventId());
            relayPointerRepository.save(pointer);
            log.info("Relayed {} event(s), pointer updated to {}", entries.size(), last.createdAt());

        } catch (Exception e) {
            log.error("Failed to relay outbox events, pointer NOT updated — will retry", e);
        }
    }
}
