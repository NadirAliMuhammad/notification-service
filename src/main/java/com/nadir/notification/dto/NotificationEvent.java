package com.nadir.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The event that flows through Kafka.
 * Designed to be channel-agnostic — EMAIL, SMS, PUSH, WEBHOOK.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @NotNull
    private Channel channel; // EMAIL, SMS, PUSH, WEBHOOK

    @NotBlank
    private String recipient; // email address, phone number, device token

    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    private Map<String, String> metadata; // optional extra data

    private int retryCount = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum Channel {
        EMAIL, SMS, PUSH, WEBHOOK
    }
}
