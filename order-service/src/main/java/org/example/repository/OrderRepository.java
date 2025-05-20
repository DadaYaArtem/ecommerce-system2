package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStatusAndRetryCountLessThanAndUpdatedAtBefore(
            String status,
            int retryCount,
            LocalDateTime updatedAt
    );
}