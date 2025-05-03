package org.example.controller;

import org.example.dto.CreateOrderRequest;
import org.example.dto.CreateOrderResponse;
import org.example.events.OrderCreatedEvent;
import org.example.messaging.OrderEventProducer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.service.TraceService;
import org.example.service.MetricsService;
import org.example.service.OrderDbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderDbService orderDbService;
    private final OrderEventProducer orderProducer;
    private final TraceService traceService;
    private final MetricsService metricsService;

    public OrderController(OrderDbService orderDbService,
                           OrderEventProducer orderProducer,
                           TraceService traceService,
                           MetricsService metricsService) {
        this.orderDbService = orderDbService;
        this.orderProducer = orderProducer;
        this.traceService = traceService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return metricsService.recordTimedOperation("createOrder", () -> {
            return traceService.traceOperation("createOrder", () -> {
                // Increment order created counter
                metricsService.incrementOrderCreated();

                // Add customer information as span attribute
                Map<String, String> attributes = new HashMap<>();
                attributes.put("customer.id", request.getCustomerId());
                attributes.put("order.items.count", String.valueOf(request.getItems().size()));
                traceService.addSpanAttributes(attributes);

                // Create order
                Order order = new Order();
                order.setCustomerId(request.getCustomerId());
                order.setStatus("CREATED");

                for (CreateOrderRequest.ItemRequest item : request.getItems()) {
                    OrderItem orderItem = new OrderItem(item.getProductId(), item.getQuantity(), order);
                    order.getItems().add(orderItem);
                }

                // Save to database
                traceService.traceOperation("saveOrderToDatabase", () -> {
                    orderDbService.save(order);
                    return null;
                });

                // Send events for each order item
                traceService.traceOperation("sendOrderEvents", () -> {
                    for (OrderItem item : order.getItems()) {
                        OrderCreatedEvent event = new OrderCreatedEvent(
                                order.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                order.getCustomerId()
                        );

                        Map<String, String> eventAttributes = new HashMap<>();
                        eventAttributes.put("product.id", item.getProductId());
                        eventAttributes.put("quantity", String.valueOf(item.getQuantity()));
                        traceService.addSpanEvent("Publishing OrderCreatedEvent", eventAttributes);

                        orderProducer.sendOrderCreatedEvent(event);
                    }
                    return null;
                });

                CreateOrderResponse response = new CreateOrderResponse(
                        order.getId(),
                        "CREATED",
                        "Order created successfully"
                );

                return ResponseEntity.ok(response);
            });
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        return metricsService.recordTimedOperation("getOrderById", () -> {
            return traceService.traceOperation("getOrderById", () -> {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("order.id", id);
                traceService.addSpanAttributes(attributes);

                return orderDbService.findById(id)
                        .map(order -> {
                            traceService.addSpanEvent("Order found", Map.of("status", order.getStatus()));
                            return ResponseEntity.ok(new CreateOrderResponse(
                                    order.getId(),
                                    order.getStatus(),
                                    "Order status fetched successfully"
                            ));
                        })
                        .orElseGet(() -> {
                            traceService.addSpanEvent("Order not found", Map.of());
                            // Increment failed counter when order is not found
                            metricsService.incrementOrderFailed();
                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(new CreateOrderResponse(id, "NOT_FOUND", "❌ Order not found"));
                        });
            });
        });
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable String id) {
        return metricsService.recordTimedOperation("payOrder", () -> {
            return traceService.traceOperation("payOrder", () -> {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("order.id", id);
                traceService.addSpanAttributes(attributes);

                return orderDbService.findById(id)
                        .map(order -> {
                            if ("CREATED".equals(order.getStatus())) {
                                order.setStatus("PAID");
                                orderDbService.save(order);
                                metricsService.incrementOrderPaid();
                                traceService.addSpanEvent("Order paid", Map.of("status", order.getStatus()));
                                return ResponseEntity.ok(new CreateOrderResponse(
                                        order.getId(),
                                        order.getStatus(),
                                        "Order successfully paid"
                                ));
                            } else {
                                traceService.addSpanEvent("Invalid order status for payment",
                                        Map.of("current_status", order.getStatus()));
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new CreateOrderResponse(
                                                order.getId(),
                                                order.getStatus(),
                                                "Order cannot be paid in current status"
                                        ));
                            }
                        })
                        .orElseGet(() -> {
                            traceService.addSpanEvent("Order not found for payment", Map.of());
                            metricsService.incrementOrderFailed();
                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(new CreateOrderResponse(id, "NOT_FOUND", "❌ Order not found"));
                        });
            });
        });
    }
}