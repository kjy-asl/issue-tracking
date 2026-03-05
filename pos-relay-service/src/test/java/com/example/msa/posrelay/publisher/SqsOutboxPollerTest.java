package com.example.msa.posrelay.publisher;

import com.example.msa.posrelay.domain.RelayPointer;
import com.example.msa.posrelay.repository.OrderOutboxEntry;
import com.example.msa.posrelay.repository.OrderOutboxReadRepository;
import com.example.msa.posrelay.repository.RelayPointerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsOutboxPollerTest {

    private static final String TEST_QUEUE_URL = "http://localhost:4566/000000000000/pos-order-queue";

    @Mock RelayPointerRepository relayPointerRepository;
    @Mock OrderOutboxReadRepository orderOutboxReadRepository;
    @Mock SqsAsyncClient sqsAsyncClient;

    SqsOutboxPoller poller;

    @BeforeEach
    void setUp() {
        poller = new SqsOutboxPoller(relayPointerRepository, orderOutboxReadRepository, sqsAsyncClient);
        ReflectionTestUtils.setField(poller, "queueName", "pos-order-queue");
        ReflectionTestUtils.setField(poller, "queueUrl", TEST_QUEUE_URL);
    }

    @Test
    void poll_처리할_이벤트가_없으면_SQS_발행_안함() {
        RelayPointer pointer = mockPointer(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
        when(relayPointerRepository.findById(1L)).thenReturn(Optional.of(pointer));
        when(orderOutboxReadRepository.findAfter(any(), anyInt())).thenReturn(List.of());

        poller.poll();

        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
        verify(relayPointerRepository, never()).save(any());
    }

    @Test
    void poll_이벤트가_있으면_SQS_발행_후_포인터_업데이트() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime t1 = since.plusSeconds(1);
        LocalDateTime t2 = since.plusSeconds(2);

        RelayPointer pointer = mockPointer(since);
        when(relayPointerRepository.findById(1L)).thenReturn(Optional.of(pointer));

        List<OrderOutboxEntry> entries = List.of(
                new OrderOutboxEntry("evt-1", "{\"orderId\":1}", t1),
                new OrderOutboxEntry("evt-2", "{\"orderId\":2}", t2)
        );
        when(orderOutboxReadRepository.findAfter(since, 50)).thenReturn(entries);
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        SendMessageResponse.builder().messageId("msg-id").build()));

        poller.poll();

        verify(sqsAsyncClient, times(2)).sendMessage(any(SendMessageRequest.class));
        verify(pointer).update(t2, "evt-2");
        verify(relayPointerRepository).save(pointer);
    }

    @Test
    void poll_SQS_발행_실패시_포인터_미업데이트() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        RelayPointer pointer = mockPointer(since);
        when(relayPointerRepository.findById(1L)).thenReturn(Optional.of(pointer));

        List<OrderOutboxEntry> entries = List.of(
                new OrderOutboxEntry("evt-1", "{\"orderId\":1}", since.plusSeconds(1))
        );
        when(orderOutboxReadRepository.findAfter(since, 50)).thenReturn(entries);

        CompletableFuture<SendMessageResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("SQS unavailable"));
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(failed);

        poller.poll(); // 예외가 내부에서 catch되어 바깥으로 전파되지 않아야 한다

        verify(pointer, never()).update(any(), any());
        verify(relayPointerRepository, never()).save(any());
    }

    @Test
    void poll_배치_최대50건_제한_파라미터_전달() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        RelayPointer pointer = mockPointer(since);
        when(relayPointerRepository.findById(1L)).thenReturn(Optional.of(pointer));
        when(orderOutboxReadRepository.findAfter(since, 50)).thenReturn(List.of());

        poller.poll();

        verify(orderOutboxReadRepository).findAfter(since, 50);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private RelayPointer mockPointer(LocalDateTime lastRelayedAt) {
        RelayPointer pointer = mock(RelayPointer.class);
        when(pointer.getLastRelayedAt()).thenReturn(lastRelayedAt);
        return pointer;
    }
}
