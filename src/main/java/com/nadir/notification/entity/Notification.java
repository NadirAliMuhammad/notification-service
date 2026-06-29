package com.nadir.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private String recipient;
    private String subject;

    @Column(length = 2000)
    private String body;

    private int retryCount;
    private String errorMessage;

    private Instant createdAt;
    private Instant processedAt;

    public enum Status { PENDING, SENT, FAILED, DEAD_LETTERED }
    public enum Channel { EMAIL, SMS, PUSH, WEBHOOK }
}
