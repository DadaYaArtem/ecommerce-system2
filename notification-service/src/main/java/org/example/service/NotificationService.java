package org.example.service;

import org.example.model.Notification;
import org.example.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> getNotificationsForOrder(String orderId) {
        return repository.findByOrderId(orderId);
    }

    public Notification createNotification(String orderId, String message) {
        Notification notification = new Notification(orderId, message);
        return repository.save(notification);
    }
}
