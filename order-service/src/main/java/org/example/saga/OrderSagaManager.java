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
        traceService.traceOperation("handleInventoryEvent", () -> {
            Map<String, String> attributes = new HashMap<>();

            if (event instanceof InventoryReservedEvent reserved) {
                attributes.put("event.type", "InventoryReservedEvent");
                attributes.put("order.id", reserved.getOrderId());
                attributes.put("product.id", reserved.getProductId());
                traceService.addSpanAttributes(attributes);

                // Check if order is still in a valid state for inventory events
                orderDbService.findById(reserved.getOrderId()).ifPresentOrElse(order -> {
                    String status = order.getStatus();

                    traceService.addSpanEvent("Order status check", Map.of("current_status", status));

                    // Ignore late inventory responses for failed orders
                    if ("FAILED_INVENTORY_TIMEOUT".equals(status)) {
                        System.out.println("⚠️ Ignoring late InventoryReservedEvent for failed order: " + reserved.getOrderId());
                        traceService.addSpanEvent("Ignoring late inventory reservation - order already failed",
                                Map.of("order_status", status));

                        // Immediately release the inventory that was just reserved
                        ReleaseInventoryEvent releaseEvent = new ReleaseInventoryEvent(
                                reserved.getOrderId(),
                                reserved.getProductId(),
                                reserved.getQuantity()
                        );
                        releaseInventoryProducer.send(releaseEvent);
                        traceService.addSpanEvent("Released late inventory reservation",
                                Map.of("product_id", reserved.getProductId()));
                        return;
                    }

                    // Process normal inventory reservation for valid orders
                    if ("CREATED".equals(status) || "INVENTORY_RETRY".equals(status)) {
                        System.out.println("✅ Saga: Inventory reserved for " + reserved.getOrderId());

                        orderDbService.updateItemStatus(reserved.getOrderId(), reserved.getProductId(), "RESERVED");
                        tryStartPricingIfReady(reserved.getOrderId());
                    } else {
                        System.out.println("⚠️ Ignoring InventoryReservedEvent for order " +
                                reserved.getOrderId() + " with status " + status);
                        traceService.addSpanEvent("Ignoring inventory reservation for order with invalid status",
                                Map.of("order_status", status));
                    }
                }, () -> {
                    // Order not found - this shouldn't happen but let's handle it gracefully
                    System.out.println("⚠️ Order not found for InventoryReservedEvent: " + reserved.getOrderId());
                    traceService.addSpanEvent("Order not found for inventory reservation", Map.of());

                    // Release the inventory since the order doesn't exist
                    ReleaseInventoryEvent releaseEvent = new ReleaseInventoryEvent(
                            reserved.getOrderId(),
                            reserved.getProductId(),
                            reserved.getQuantity()
                    );
                    releaseInventoryProducer.send(releaseEvent);
                });

            } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
                attributes.put("event.type", "InventoryNotAvailableEvent");
                attributes.put("order.id", notAvailable.getOrderId());
                attributes.put("product.id", notAvailable.getProductId());
                traceService.addSpanAttributes(attributes);

                // Check if order is still in a valid state for inventory events
                orderDbService.findById(notAvailable.getOrderId()).ifPresent(order -> {
                    String status = order.getStatus();

                    traceService.addSpanEvent("Order status check", Map.of("current_status", status));

                    // Ignore late inventory responses for failed orders
                    if ("FAILED_INVENTORY_TIMEOUT".equals(status)) {
                        System.out.println("⚠️ Ignoring late InventoryNotAvailableEvent for failed order: " + notAvailable.getOrderId());
                        traceService.addSpanEvent("Ignoring late inventory not available - order already failed",
                                Map.of("order_status", status));
                        return;
                    }

                    // Process normal inventory not available for valid orders
                    if ("CREATED".equals(status) || "INVENTORY_RETRY".equals(status)) {
                        System.out.println("❌ Saga: Inventory not available for " + notAvailable.getOrderId());

                        orderDbService.updateItemStatus(notAvailable.getOrderId(), notAvailable.getProductId(), "FAILED_RESERVE");

                        // Mark order as failed only if we're not in retry mode
                        if (!"INVENTORY_RETRY".equals(status)) {
                            orderDbService.updateStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");
                            System.out.println("❌ Статус FAILED_INVENTORY: деякі товари недоступні");

                            // Release any inventory that was already reserved
                            order.getItems().stream()
                                    .filter(item -> "RESERVED".equals(item.getStatus()))
                                    .forEach(item -> {
                                        ReleaseInventoryEvent releaseEvent = new ReleaseInventoryEvent(
                                                order.getId(),
                                                item.getProductId(),
                                                item.getQuantity()
                                        );
                                        releaseInventoryProducer.send(releaseEvent);
                                        traceService.addSpanEvent("Released previously reserved inventory",
                                                Map.of("product_id", item.getProductId()));
                                    });
                        }
                    } else {
                        System.out.println("⚠️ Ignoring InventoryNotAvailableEvent for order " +
                                notAvailable.getOrderId() + " with status " + status);
                        traceService.addSpanEvent("Ignoring inventory not available for order with invalid status",
                                Map.of("order_status", status));
                    }
                });

            } else if (event instanceof InventoryErrorEvent errorEvent) {
                // Handle inventory service errors
                attributes.put("event.type", "InventoryErrorEvent");
                attributes.put("order.id", errorEvent.getOrderId());
                attributes.put("error.message", errorEvent.getErrorMessage());
                traceService.addSpanAttributes(attributes);

                // Check if order is still in a valid state for inventory events
                orderDbService.findById(errorEvent.getOrderId()).ifPresent(order -> {
                    String status = order.getStatus();

                    // Ignore late inventory responses for failed orders
                    if ("FAILED_INVENTORY_TIMEOUT".equals(status)) {
                        System.out.println("⚠️ Ignoring late InventoryErrorEvent for failed order: " + errorEvent.getOrderId());
                        traceService.addSpanEvent("Ignoring late inventory error - order already failed",
                                Map.of("order_status", status));
                        return;
                    }

                    System.out.println("⚠️ Saga: Inventory service error for " + errorEvent.getOrderId() +
                            ": " + errorEvent.getErrorMessage());

                    // We don't immediately fail the order on error, as the timeout mechanism will retry
                    // This allows the inventory service to recover without failing orders
                    traceService.addSpanEvent("Inventory service error - will rely on timeout retry mechanism",
                            Map.of("error_message", errorEvent.getErrorMessage()));
                });
            }

            return null;
        });
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
