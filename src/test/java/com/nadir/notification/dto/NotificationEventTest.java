package com.nadir.notification.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEventTest {

    @Test
    void builder_generatesEventIdAndDefaults() {
        NotificationEvent event = NotificationEvent.builder()
                .channel(NotificationEvent.Channel.EMAIL)
                .recipient("user@example.com")
                .subject("Welcome")
                .body("Thanks for signing up")
                .build();

        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getRetryCount()).isZero();
    }

    @Test
    void builder_generatesUniqueEventIdsPerEvent() {
        NotificationEvent first = NotificationEvent.builder().build();
        NotificationEvent second = NotificationEvent.builder().build();

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }
}
