package org.example.controller;

import org.example.dto.NotificationResponse;
import org.example.model.Notification;
import org.example.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable String orderId) {
        List<Notification> notifications = notificationService.getNotificationsForOrder(orderId);
        List<NotificationResponse> response = notifications.stream()
                .map(n -> new NotificationResponse(n.getId(), n.getOrderId(), n.getMessage(), n.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/test")
    public ResponseEntity<NotificationResponse> sendTestNotification() {
        Notification notification = notificationService.createNotification("test-order", "Test notification");
        NotificationResponse response = new NotificationResponse(
                notification.getId(), notification.getOrderId(), notification.getMessage(), notification.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }
}
