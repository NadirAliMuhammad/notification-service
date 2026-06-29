package com.nadir.notification.producer;

import com.nadir.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    public static final String TOPIC = "notifications";
    public static final String DLQ_TOPIC = "notifications.dlq";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    /**
     * Publish notification event to Kafka.
     * Uses eventId as partition key — ensures ordering per notification.
     */
    public void send(NotificationEvent event) {
        CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(TOPIC, event.getEventId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Notification sent to Kafka — eventId: {}, channel: {}, offset: {}",
                        event.getEventId(), event.getChannel(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send notification to Kafka — eventId: {}, error: {}",
                        event.getEventId(), ex.getMessage());
            }
        });
    }

    /**
     * Send to Dead Letter Queue after max retries exhausted.
     */
    public void sendToDlq(NotificationEvent event) {
        log.warn("Sending to DLQ — eventId: {}, retryCount: {}",
                event.getEventId(), event.getRetryCount());
        kafkaTemplate.send(DLQ_TOPIC, event.getEventId(), event);
    }
}
