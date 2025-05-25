package org.example.service;

import org.example.events.OrderCreatedEvent;
import org.example.messaging.OrderEventProducer;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.events.ReleaseInventoryEvent;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to handle timeouts for inventory operations.
 * Detects orders that have been waiting for inventory responses for too long
 * and applies appropriate retry or failure logic.
 */
@Service
public class InventoryTimeoutService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;
    private final MetricsService metricsService;
    private final TraceService traceService;

    @Value("${app.inventory.timeout-minutes:1}")
    private int inventoryTimeoutMinutes;

    @Value("${app.inventory.max-retries:1}")
    private int maxRetries;

    public InventoryTimeoutService(
            OrderRepository orderRepository,
            OrderEventProducer orderEventProducer,
            ReleaseInventoryEventProducer releaseInventoryProducer,
            MetricsService metricsService,
            TraceService traceService) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.releaseInventoryProducer = releaseInventoryProducer;
        this.metricsService = metricsService;
        this.traceService = traceService;
    }

    /**
     * Schedule a task that runs every minute to check for inventory operations that have timed out
     */
    @Scheduled(fixedRate = 60000)  // Run every 60 seconds
    public void checkForTimedOutInventoryOperations() {
        traceService.traceOperation("checkForTimedOutInventoryOperations", () -> {
            findTimedOutOrders().forEach(this::handleTimedOutInventoryOrder);
            return null;
        });
    }

    /**
     * Find orders that are waiting for inventory reservation and have exceeded the timeout period
     */
    private List<Order> findTimedOutOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(inventoryTimeoutMinutes, ChronoUnit.MINUTES);

        return orderRepository.findByStatusAndRetryCountLessThanAndUpdatedAtBefore(
                "CREATED",  // Orders still in CREATED state waiting for inventory
                maxRetries,
                cutoffTime
        );
    }

    /**
     * Handle an order with timed-out inventory operations based on retry count
     */
    private void handleTimedOutInventoryOrder(Order order) {
        traceService.traceOperation("handleTimedOutInventoryOrder", () -> {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("order.id", order.getId());
            attributes.put("retry.count", String.valueOf(order.getRetryCount()));
            traceService.addSpanAttributes(attributes);

            // Increment retry count
            order.setRetryCount(order.getRetryCount() + 1);

            boolean hasItemsWaitingForInventory = order.getItems().stream()
                    .anyMatch(item -> "CREATED".equals(item.getStatus()));

            if (!hasItemsWaitingForInventory) {
                // No items waiting for inventory, this order might be in an inconsistent state
                // or being processed by another mechanism
                traceService.addSpanEvent("Order has no items waiting for inventory", Map.of());
                return null;
            }

            if (order.getRetryCount() >= maxRetries) {
                // Max retries reached, mark order as failed
                order.setStatus("FAILED_INVENTORY_TIMEOUT");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                // Add event to trace
                traceService.addSpanEvent("Inventory reservation failed after max retries",
                        Map.of("max_retries", String.valueOf(maxRetries)));

                // Increment metrics
                metricsService.incrementOrderFailed();
                metricsService.incrementInventoryTimeout();

                // Release any inventory that might have been reserved by late responses
                // This is a precautionary measure since inventory service might respond late
                releaseAnyReservedInventory(order);

            } else {
                // Update status for retry
                order.setStatus("INVENTORY_RETRY");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                // Add event to trace
                traceService.addSpanEvent("Retrying inventory reservation",
                        Map.of("retry_count", String.valueOf(order.getRetryCount())));

                // Increment retry metric
                metricsService.incrementInventoryRetry();

                // Retry inventory reservation for items still in CREATED state
                for (OrderItem item : order.getItems()) {
                    if ("CREATED".equals(item.getStatus())) {
                        OrderCreatedEvent event = new OrderCreatedEvent(
                                order.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                order.getCustomerId()
                        );
                        orderEventProducer.sendOrderCreatedEvent(event);

                        traceService.addSpanEvent("Resent OrderCreatedEvent",
                                Map.of("product_id", item.getProductId()));
                    }
                }

                // Update status back to CREATED to continue normal flow
                order.setStatus("CREATED");
                orderRepository.save(order);
            }

            return null;
        });
    }

    /**
     * Release any inventory that might have been reserved for this order.
     * This is called when an order fails due to inventory timeout to ensure
     * that any late responses from inventory service that reserved items are compensated.
     */
    private void releaseAnyReservedInventory(Order order) {
        traceService.traceOperation("releaseAnyReservedInventory", () -> {
            // Send release events for all items in the order as a precaution
            // The inventory service should handle duplicate releases gracefully
            for (OrderItem item : order.getItems()) {
                ReleaseInventoryEvent releaseEvent = new ReleaseInventoryEvent(
                        order.getId(),
                        item.getProductId(),
                        item.getQuantity()
                );
                releaseInventoryProducer.send(releaseEvent);

                // Update item status to reflect that we've attempted compensation
                item.setStatus("INVENTORY_RELEASED");

                traceService.addSpanEvent("Sent compensating inventory release",
                        Map.of("product_id", item.getProductId(),
                                "quantity", String.valueOf(item.getQuantity())));
            }

            // Save the order with updated item statuses
            orderRepository.save(order);
            return null;
        });
    }
}