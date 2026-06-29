package com.nadir.notification.consumer;

import com.nadir.notification.dto.NotificationEvent;
import com.nadir.notification.entity.Notification;
import com.nadir.notification.producer.NotificationProducer;
import com.nadir.notification.repository.NotificationRepository;
import com.nadir.notification.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final int MAX_RETRIES = 3;

    private final NotificationDispatcher dispatcher;
    private final NotificationRepository notificationRepository;
    private final NotificationProducer producer;

    /**
     * Main consumer — processes notifications from Kafka.
     *
     * @RetryableTopic handles automatic retries with exponential backoff.
     * After MAX_RETRIES, sends to DLQ automatically.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = ".dlq"
    )
    @KafkaListener(topics = NotificationProducer.TOPIC, groupId = "notification-group")
    public void consume(NotificationEvent event) {
        log.info("Processing notification — eventId: {}, channel: {}, attempt: {}",
                event.getEventId(), event.getChannel(), event.getRetryCount() + 1);

        Notification notification = saveOrUpdate(event, Notification.Status.PENDING);

        try {
            dispatcher.dispatch(event);
            notification.setStatus(Notification.Status.SENT);
            notification.setProcessedAt(Instant.now());
            notificationRepository.save(notification);
            log.info("Notification sent — eventId: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process notification — eventId: {}, error: {}",
                    event.getEventId(), e.getMessage());

            notification.setRetryCount(event.getRetryCount() + 1);
            notification.setErrorMessage(e.getMessage());

            if (notification.getRetryCount() >= MAX_RETRIES) {
                notification.setStatus(Notification.Status.DEAD_LETTERED);
                notificationRepository.save(notification);
                throw new RuntimeException("Max retries exceeded", e); // triggers DLQ
            }

            notification.setStatus(Notification.Status.FAILED);
            notificationRepository.save(notification);
            throw e; // triggers retry
        }
    }

    /**
     * DLQ consumer — logs and stores failed notifications for manual review.
     */
    @KafkaListener(topics = NotificationProducer.DLQ_TOPIC, groupId = "notification-dlq-group")
    public void consumeDlq(NotificationEvent event) {
        log.error("DLQ notification received — eventId: {}, channel: {}, retries: {}",
                event.getEventId(), event.getChannel(), event.getRetryCount());

        Notification notification = saveOrUpdate(event, Notification.Status.DEAD_LETTERED);
        notification.setErrorMessage("Moved to DLQ after " + event.getRetryCount() + " retries");
        notificationRepository.save(notification);

        // In production: alert on-call, create incident ticket, send to monitoring
    }

    private Notification saveOrUpdate(NotificationEvent event, Notification.Status status) {
        return notificationRepository.findByEventId(event.getEventId())
                .orElseGet(() -> notificationRepository.save(Notification.builder()
                        .eventId(event.getEventId())
                        .status(status)
                        .channel(Notification.Channel.valueOf(event.getChannel().name()))
                        .recipient(event.getRecipient())
                        .subject(event.getSubject())
                        .body(event.getBody())
                        .retryCount(event.getRetryCount())
                        .createdAt(event.getCreatedAt())
                        .build()));
    }
}
