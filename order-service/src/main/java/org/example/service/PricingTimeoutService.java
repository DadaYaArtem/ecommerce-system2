package org.example.service;

import org.example.events.ReleaseInventoryEvent;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.model.Order;
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
 * Service to handle timeouts for pricing operations.
 * Detects orders that have been waiting for pricing responses for too long
 * and applies appropriate failure logic with inventory compensation.
 */
@Service
public class PricingTimeoutService {

    private final OrderRepository orderRepository;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;
    private final MetricsService metricsService;
    private final TraceService traceService;

    @Value("${app.pricing.timeout-minutes:2}")
    private int pricingTimeoutMinutes;

    public PricingTimeoutService(
            OrderRepository orderRepository,
            ReleaseInventoryEventProducer releaseInventoryProducer,
            MetricsService metricsService,
            TraceService traceService) {
        this.orderRepository = orderRepository;
        this.releaseInventoryProducer = releaseInventoryProducer;
        this.metricsService = metricsService;
        this.traceService = traceService;
    }

    /**
     * Schedule a task that runs every minute to check for pricing operations that have timed out
     */
    @Scheduled(fixedRate = 60000)  // Run every 60 seconds
    public void checkForTimedOutPricingOperations() {
        traceService.traceOperation("checkForTimedOutPricingOperations", () -> {
            findTimedOutPricingOrders().forEach(this::handleTimedOutPricingOrder);
            return null;
        });
    }

    /**
     * Find orders that have reserved inventory but are waiting for pricing responses
     * and have exceeded the timeout period
     */
    private List<Order> findTimedOutPricingOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(pricingTimeoutMinutes, ChronoUnit.MINUTES);

        // Find orders that are still in CREATED status (not moved to AWAITING_PAYMENT yet)
        // and have been updated before the cutoff time
        return orderRepository.findByStatusAndRetryCountLessThanAndUpdatedAtBefore(
                "CREATED",
                1, // We don't retry pricing requests - if pricing service is down, fail immediately
                cutoffTime
        );
    }

    /**
     * Handle an order with timed-out pricing operations
     */
    private void handleTimedOutPricingOrder(Order order) {
        traceService.traceOperation("handleTimedOutPricingOrder", () -> {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("order.id", order.getId());
            traceService.addSpanAttributes(attributes);

            // Check if this order actually has reserved inventory waiting for pricing
            boolean hasReservedItems = order.getItems().stream()
                    .anyMatch(item -> "RESERVED".equals(item.getStatus()));

            if (!hasReservedItems) {
                // This order is not waiting for pricing, skip it
                traceService.addSpanEvent("Order has no reserved items waiting for pricing", Map.of());
                return null;
            }

            // Check if all items already have prices (late response might have arrived)
            boolean allItemsPriced = order.getItems().stream()
                    .allMatch(item -> item.getPrice() != null);

            if (allItemsPriced) {
                // Pricing completed, this order is being processed normally
                traceService.addSpanEvent("All items already priced, skipping timeout", Map.of());
                return null;
            }

            System.out.println("⏰ Pricing timeout detected for order: " + order.getId());

            // Mark order as failed due to pricing timeout
            order.setStatus("FAILED_PRICING_TIMEOUT");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Add event to trace
            traceService.addSpanEvent("Pricing service timeout - order failed",
                    Map.of("timeout_minutes", String.valueOf(pricingTimeoutMinutes)));

            // Increment metrics
            metricsService.incrementOrderFailed();
            metricsService.incrementPricingTimeout();

            // Release all reserved inventory as compensation
            releaseReservedInventory(order);

            return null;
        });
    }

    /**
     * Release all reserved inventory for an order
     */
    private void releaseReservedInventory(Order order) {
        order.getItems().stream()
                .filter(item -> "RESERVED".equals(item.getStatus()))
                .forEach(item -> {
                    ReleaseInventoryEvent event = new ReleaseInventoryEvent(
                            order.getId(),
                            item.getProductId(),
                            item.getQuantity()
                    );
                    releaseInventoryProducer.send(event);

                    // Update item status to reflect that inventory was released
                    item.setStatus("INVENTORY_RELEASED");

                    System.out.println("🔄 Released inventory for product: " + item.getProductId() +
                            ", quantity: " + item.getQuantity());

                    // Add trace event for each released item
                    Map<String, String> releaseAttributes = new HashMap<>();
                    releaseAttributes.put("product.id", item.getProductId());
                    releaseAttributes.put("quantity", String.valueOf(item.getQuantity()));
                    traceService.addSpanEvent("Released reserved inventory due to pricing timeout",
                            releaseAttributes);
                });

        // Save order with updated item statuses
        orderRepository.save(order);
    }
}