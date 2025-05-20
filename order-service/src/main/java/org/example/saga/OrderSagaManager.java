package org.example.saga;

import org.example.events.*;
import org.example.messaging.PriceRequestProducer;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.service.MetricsService;
import org.example.service.OrderDbService;
import org.example.service.TraceService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderSagaManager {

    private final MetricsService metricsService;
    private final OrderDbService orderDbService;
    private final PriceRequestProducer priceRequestProducer;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;
    private final TraceService traceService;

    public OrderSagaManager(MetricsService metricsService, OrderDbService orderDbService,
                            PriceRequestProducer priceRequestProducer,
                            ReleaseInventoryEventProducer releaseInventoryProducer, TraceService traceService) {
        this.metricsService = metricsService;
        this.orderDbService = orderDbService;
        this.priceRequestProducer = priceRequestProducer;
        this.releaseInventoryProducer = releaseInventoryProducer;
        this.traceService = traceService;
    }

    public void handleInventoryEvent(Object event) {
        if (event instanceof InventoryReservedEvent reserved) {
            System.out.println("✅ Saga: Inventory reserved for " + reserved.getOrderId());

            orderDbService.updateItemStatus(reserved.getOrderId(), reserved.getProductId(), "RESERVED");

            tryStartPricingIfReady(reserved.getOrderId());

        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            System.out.println("❌ Saga: Inventory not available for " + notAvailable.getOrderId());

            orderDbService.updateItemStatus(notAvailable.getOrderId(), notAvailable.getProductId(), "FAILED_RESERVE");

            orderDbService.updateStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");

            System.out.println("❌ Статус FAILED_INVENTORY: деякі товари недоступні");
        }
    }

    public void handlePaymentEvent(Object event) {
        traceService.traceOperation("handlePaymentEvent", () -> {
            Map<String, String> attributes = new HashMap<>();

            if (event instanceof PaymentConfirmedEvent confirmed) {
                attributes.put("event.type", "PaymentConfirmedEvent");
                attributes.put("order.id", confirmed.getOrderId());
                traceService.addSpanAttributes(attributes);

                // Check if the order is still in a valid state for payment confirmation
                orderDbService.findById(confirmed.getOrderId()).ifPresent(order -> {
                    String status = order.getStatus();

                    traceService.addSpanEvent("Order status check", Map.of("current_status", status));

                    // Only process payment confirmation for orders awaiting payment
                    if ("AWAITING_PAYMENT".equals(status) || "PAYMENT_RETRY".equals(status)) {
                        System.out.println("✅ Payment successful for order: " + confirmed.getOrderId());

                        orderDbService.updateStatus(confirmed.getOrderId(), "PAID");
                        metricsService.incrementOrderPaid();
                    } else {
                        // Order is not in a valid state for payment confirmation
                        System.out.println("⚠️ Ignoring payment confirmation for order " +
                                confirmed.getOrderId() + " with status " + status);
                        traceService.addSpanEvent("Ignoring stale payment confirmation",
                                Map.of("order_status", status));
                    }
                });
            } else if (event instanceof PaymentFailedEvent failed) {
                attributes.put("event.type", "PaymentFailedEvent");
                attributes.put("order.id", failed.getOrderId());
                traceService.addSpanAttributes(attributes);

                // Check if the order is still in a valid state for payment failure
                orderDbService.findById(failed.getOrderId()).ifPresent(order -> {
                    String status = order.getStatus();

                    traceService.addSpanEvent("Order status check", Map.of("current_status", status));

                    // Only process payment failure for orders awaiting payment
                    if ("AWAITING_PAYMENT".equals(status) || "PAYMENT_RETRY".equals(status)) {
                        System.out.println("❌ Payment failed: " + failed.getOrderId());

                        orderDbService.updateStatus(failed.getOrderId(), "FAILED_PAYMENT");

                        // Release inventory
                        order.getItems().stream()
                                .filter(item -> "RESERVED".equals(item.getStatus()))
                                .forEach(item -> releaseInventoryProducer.send(new ReleaseInventoryEvent(
                                        order.getId(),
                                        item.getProductId(),
                                        item.getQuantity()
                                )));
                    } else {
                        // Order is not in a valid state for payment failure
                        System.out.println("⚠️ Ignoring payment failure for order " +
                                failed.getOrderId() + " with status " + status);
                        traceService.addSpanEvent("Ignoring stale payment failure",
                                Map.of("order_status", status));
                    }
                });
            }

            return null;
        });
    }

    public void tryStartPricingIfReady(String orderId) {
        if (orderDbService.allItemsReserved(orderId)) {
            System.out.println("✅ Всі товари зарезервовані. Надсилаємо запити цін.");

            orderDbService.findById(orderId).ifPresent(order -> {
                order.getItems().forEach(item -> {
                    priceRequestProducer.sendPriceRequest(new PriceRequestEvent(
                            order.getId(),
                            item.getProductId()
                    ));
                    System.out.println("📤 Надіслано PriceRequestEvent: " + item.getProductId());
                });
            });
        }
    }
}
