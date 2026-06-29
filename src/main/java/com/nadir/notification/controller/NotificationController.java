package com.nadir.notification.controller;

import com.nadir.notification.dto.NotificationEvent;
import com.nadir.notification.entity.Notification;
import com.nadir.notification.producer.NotificationProducer;
import com.nadir.notification.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationProducer producer;
    private final NotificationRepository repository;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@Valid @RequestBody NotificationEvent event) {
        producer.send(event);
        return ResponseEntity.accepted().body(Map.of(
                "message", "Notification queued",
                "eventId", event.getEventId(),
                "channel", event.getChannel()
        ));
    }

    @PostMapping("/send/bulk")
    public ResponseEntity<Map<String, Object>> sendBulk(@RequestBody List<NotificationEvent> events) {
        events.forEach(producer::send);
        return ResponseEntity.accepted().body(Map.of(
                "message", "Bulk notifications queued",
                "count", events.size()
        ));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Notification>> getFailed() {
        return ResponseEntity.ok(repository.findByStatus(Notification.Status.FAILED));
    }

    @GetMapping("/dlq")
    public ResponseEntity<List<Notification>> getDlq() {
        return ResponseEntity.ok(repository.findByStatus(Notification.Status.DEAD_LETTERED));
    }
}
