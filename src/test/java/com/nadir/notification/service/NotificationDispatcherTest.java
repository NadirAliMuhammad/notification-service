package com.nadir.notification.service;

import com.nadir.notification.dto.NotificationEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationDispatcherTest {

    private final NotificationDispatcher dispatcher = new NotificationDispatcher();

    @Test
    void dispatch_handlesEveryChannelWithoutError() {
        for (NotificationEvent.Channel channel : NotificationEvent.Channel.values()) {
            NotificationEvent event = NotificationEvent.builder()
                    .channel(channel)
                    .recipient("recipient")
                    .subject("subject")
                    .body("a short body")
                    .build();

            assertThatCode(() -> dispatcher.dispatch(event)).doesNotThrowAnyException();
        }
    }

    @Test
    void dispatch_truncatesLongSmsBodySafely() {
        NotificationEvent event = NotificationEvent.builder()
                .channel(NotificationEvent.Channel.SMS)
                .recipient("+15551234567")
                .subject("OTP")
                .body("x".repeat(500)) // longer than the 50-char preview window
                .build();

        assertThatCode(() -> dispatcher.dispatch(event)).doesNotThrowAnyException();
    }
}
