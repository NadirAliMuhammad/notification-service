# Event-Driven Notification Service

An **event-driven notification service** built with Spring Boot 3 and Apache Kafka — featuring automatic retries with exponential backoff and a Dead Letter Queue (DLQ) for failed messages. It models the messaging backbone systems use to send email, SMS, push, and webhook notifications reliably.

## Architecture

```
HTTP Request
    ↓
NotificationController  (POST /api/notifications/send)
    ↓
NotificationProducer    → Kafka topic "notifications"
    ↓
NotificationConsumer    (@RetryableTopic: 3 attempts, exponential backoff)
    ↓
NotificationDispatcher  → EMAIL | SMS | PUSH | WEBHOOK
    ↓
On repeated failure → Dead Letter Queue ("notifications.dlq")
```

## Features

- ✅ **Channel-agnostic events** — EMAIL, SMS, PUSH, WEBHOOK from one API
- ✅ **Kafka producer/consumer** with JSON serialization
- ✅ **Automatic retries** — `@RetryableTopic` with exponential backoff (1s → 2s → 4s)
- ✅ **Dead Letter Queue** — failed messages are isolated for manual review
- ✅ **Idempotent processing** — deduplicated by `eventId`
- ✅ **Status tracking** — PENDING → SENT / FAILED / DEAD_LETTERED persisted to DB
- ✅ **Bulk send** endpoint
- ✅ Docker Compose (Kafka + Zookeeper + app)

## Running

```bash
docker-compose up --build
```

This starts Zookeeper, Kafka, and the service on port `8084`.

## Tests

```bash
mvn test
```

Unit tests cover channel dispatch across all notification types and event/idempotency-key generation. (Kafka end-to-end flows run against the Docker Compose stack.)

## API Usage

### Send a single notification
```bash
curl -X POST http://localhost:8084/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
        "channel": "EMAIL",
        "recipient": "user@example.com",
        "subject": "Welcome!",
        "body": "Thanks for signing up."
      }'
```

### Send in bulk
```bash
curl -X POST http://localhost:8084/api/notifications/send/bulk \
  -H "Content-Type: application/json" \
  -d '[
        {"channel":"SMS","recipient":"+15551234567","subject":"OTP","body":"Your code is 123456"},
        {"channel":"PUSH","recipient":"device-token","subject":"Reminder","body":"Meeting at 3pm"}
      ]'
```

### Inspect processing state
```bash
curl http://localhost:8084/api/notifications          # all
curl http://localhost:8084/api/notifications/failed    # failed
curl http://localhost:8084/api/notifications/dlq       # dead-lettered
```

## Why a Dead Letter Queue?

When a notification fails after all retries (e.g. a downstream provider is down), blindly retrying forever blocks the queue and loses observability. The DLQ isolates these messages so the rest of the pipeline keeps flowing, while failed events are preserved for inspection, alerting, and replay.

## Production Considerations

- Wire `NotificationDispatcher` to real providers (SendGrid, Twilio, FCM/APNs)
- Alert on DLQ arrivals (PagerDuty/Slack) and build a replay tool
- Replace H2 with a durable database
- Tune partitions and consumer concurrency for throughput
- Add schema versioning for the event payload

## Author
**Muhammad Nadir** — [LinkedIn](https://linkedin.com/in/muhammad-nadir-26095646)
