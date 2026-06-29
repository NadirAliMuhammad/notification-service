package com.nadir.notification.repository;
import com.nadir.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByEventId(String eventId);
    List<Notification> findByStatus(Notification.Status status);
    List<Notification> findByRecipient(String recipient);
}
