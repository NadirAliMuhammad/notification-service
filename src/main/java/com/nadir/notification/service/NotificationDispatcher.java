package com.nadir.notification.service;

import com.nadir.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Routes notifications to the correct channel handler.
 * In production: inject real email/SMS/push clients here.
 */
@Slf4j
@Service
public class NotificationDispatcher {

    public void dispatch(NotificationEvent event) {
        log.info("Dispatching via channel: {}", event.getChannel());
        switch (event.getChannel()) {
            case EMAIL   -> sendEmail(event);
            case SMS     -> sendSms(event);
            case PUSH    -> sendPush(event);
            case WEBHOOK -> sendWebhook(event);
        }
    }

    private void sendEmail(NotificationEvent event) {
        // Production: inject JavaMailSender or SendGrid client
        log.info("📧 EMAIL → to: {}, subject: {}", event.getRecipient(), event.getSubject());
        simulateDelay();
    }

    private void sendSms(NotificationEvent event) {
        // Production: inject Twilio client
        log.info("📱 SMS → to: {}, message: {}", event.getRecipient(),
                event.getBody().substring(0, Math.min(50, event.getBody().length())));
        simulateDelay();
    }

    private void sendPush(NotificationEvent event) {
        // Production: inject FCM/APNs client
        log.info("🔔 PUSH → token: {}, title: {}", event.getRecipient(), event.getSubject());
        simulateDelay();
    }

    private void sendWebhook(NotificationEvent event) {
        // Production: inject RestTemplate/WebClient
        log.info("🔗 WEBHOOK → url: {}", event.getRecipient());
        simulateDelay();
    }

    private void simulateDelay() {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    }
}
