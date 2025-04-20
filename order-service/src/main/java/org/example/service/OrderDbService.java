package org.example.service;


import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderDbService {

    private final OrderRepository repository;

    public OrderDbService(OrderRepository repository) {
        this.repository = repository;
    }

    public void save(Order order) {
        repository.save(order);
    }

    public Optional<Order> findById(String id) {
        return repository.findById(id);
    }

    @Transactional
    public void updateStatus(String orderId, String status) {
        repository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            repository.save(order);
        });
    }

    @Transactional
    public void updatePrice(String orderId, double price) {
        repository.findById(orderId).ifPresent(order -> {
            order.setPrice(price);
            repository.save(order);
        });
    }

    @Transactional
    public void updateItemStatus(String orderId, String productId, String status) {
        repository.findById(orderId).ifPresent(order -> {
            order.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setStatus(status);
                        repository.save(order);
                    });
        });
    }

    @Transactional
    public void updateItemPrice(String orderId, String productId, double price) {
        repository.findById(orderId).ifPresent(order -> {
            order.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setPrice(price);
                        repository.save(order);
                    });
        });
    }

    public boolean allItemsPriced(String orderId) {
        return repository.findById(orderId)
                .map(order -> order.getItems().stream()
                        .allMatch(item -> item.getPrice() != null))
                .orElse(false);
    }

    public double calculateTotalAmount(String orderId) {
        return repository.findById(orderId)
                .map(order -> order.getItems().stream()
                        .filter(item -> item.getPrice() != null)
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum())
                .orElse(0.0);
    }

    public boolean allItemsReserved(String orderId) {
        return repository.findById(orderId)
                .map(order -> order.getItems().stream()
                        .allMatch(item -> "RESERVED".equals(item.getStatus())))
                .orElse(false);
    }

//    public boolean hasAnyItemFailedReserve(String orderId) {
//        return repository.findById(orderId)
//                .map(order -> order.getItems().stream()
//                        .anyMatch(item -> "FAILED_RESERVE".equals(item.getStatus())))
//                .orElse(false);
//    }
}
