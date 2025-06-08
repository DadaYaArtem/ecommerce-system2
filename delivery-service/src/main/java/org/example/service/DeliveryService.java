package org.example.service;

import org.example.model.Delivery;
import org.example.model.DeliveryStatus;
import org.example.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DeliveryService {

    private final DeliveryRepository repository;

    public DeliveryService(DeliveryRepository repository) {
        this.repository = repository;
    }

    public Delivery createDelivery(String orderId, double amount) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setAmount(amount);
        return repository.save(delivery);
    }

    public Delivery findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    @Transactional
    public void updateStatus(String orderId, DeliveryStatus status) {
        Delivery delivery = repository.findByOrderId(orderId);
        if (delivery != null) {
            delivery.setStatus(status);
            delivery.setUpdatedAt(LocalDateTime.now());
            repository.save(delivery);
        }
    }
}
