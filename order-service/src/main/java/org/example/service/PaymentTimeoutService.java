package org.example.service;

import org.example.events.PaymentRequestEvent;
import org.example.events.ReleaseInventoryEvent;
import org.example.messaging.PaymentRequestEventProducer;
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

@Service
public class PaymentTimeoutService {

    private final OrderRepository orderRepository;
    private final OrderDbService orderDbService;
    private final PaymentRequestEventProducer paymentProducer;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;
    private final MetricsService metricsService;
    private final TraceService traceService;
    
    @Value("${app.payment.timeout-minutes:5}")
    private int paymentTimeoutMinutes;
    
    @Value("${app.payment.max-retries:3}")
    private int maxRetries;

    public PaymentTimeoutService(
            OrderRepository orderRepository,
            OrderDbService orderDbService,
            PaymentRequestEventProducer paymentProducer,
            ReleaseInventoryEventProducer releaseInventoryProducer,
            MetricsService metricsService,
            TraceService traceService) {
        this.orderRepository = orderRepository;
        this.orderDbService = orderDbService;
        this.paymentProducer = paymentProducer;
        this.releaseInventoryProducer = releaseInventoryProducer;
        this.metricsService = metricsService;
        this.traceService = traceService;
    }

    /**
     * Schedule a task that runs every minute to check for timed-out payments
     */
    @Scheduled(fixedRate = 60000)  // Run every 60 seconds
    public void checkForTimedOutPayments() {
        traceService.traceOperation("checkForTimedOutPayments", () -> {
            findTimedOutOrders().forEach(this::handleTimedOutOrder);
            return null;
        });
    }
    
    /**
     * Find orders that are waiting for payment and have exceeded the timeout period
     */
    private List<Order> findTimedOutOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(paymentTimeoutMinutes, ChronoUnit.MINUTES);
        
        return orderRepository.findByStatusAndRetryCountLessThanAndUpdatedAtBefore(
                "AWAITING_PAYMENT", 
                maxRetries, 
                cutoffTime
        );
    }
    
    /**
     * Handle a timed-out order based on retry count
     */
    private void handleTimedOutOrder(Order order) {
        traceService.traceOperation("handleTimedOutOrder", () -> {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("order.id", order.getId());
            attributes.put("retry.count", String.valueOf(order.getRetryCount()));
            traceService.addSpanAttributes(attributes);
            
            // Increment retry count
            order.setRetryCount(order.getRetryCount() + 1);
            
            if (order.getRetryCount() >= maxRetries) {
                // Max retries reached, mark order as failed
                order.setStatus("FAILED_PAYMENT_TIMEOUT");
                orderRepository.save(order);
                
                // Add event to trace
                traceService.addSpanEvent("Payment failed after max retries", 
                        Map.of("max_retries", String.valueOf(maxRetries)));
                
                // Increment metrics
                metricsService.incrementOrderFailed();
                metricsService.incrementPaymentTimeout();
                
                // Release reserved inventory
                releaseReservedInventory(order);
            } else {
                // Retry payment
                order.setStatus("PAYMENT_RETRY");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                // Add event to trace
                traceService.addSpanEvent("Retrying payment", 
                        Map.of("retry_count", String.valueOf(order.getRetryCount())));
                
                // Retry the payment request
                double total = orderDbService.calculateTotalAmount(order.getId());
                PaymentRequestEvent event = new PaymentRequestEvent(
                        order.getId(),
                        total,
                        order.getCustomerId()
                );
                paymentProducer.sendPaymentRequest(event);
                
                // Update status to awaiting payment again
                order.setStatus("AWAITING_PAYMENT");
                orderRepository.save(order);
            }
            
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
                    
                    // Update item status
                    item.setStatus("INVENTORY_RELEASED");
                });
        
        // Save order with updated item statuses
        orderRepository.save(order);
    }
}