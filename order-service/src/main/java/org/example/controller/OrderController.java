package org.example.controller;

import org.example.dto.CreateOrderRequest;
import org.example.dto.CreateOrderResponse;
import org.example.event.OrderCreatedEvent;
import org.example.messaging.OrderEventProducer;
import org.example.store.OrderStatusStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderEventProducer eventProducer;
    private final OrderStatusStore orderStatusStore;

    public OrderController(OrderEventProducer eventProducer, OrderStatusStore orderStatusStore) {
        this.eventProducer = eventProducer;
        this.orderStatusStore = orderStatusStore;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        orderStatusStore.setStatus(orderId, "CREATED");

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getCustomerId()
        );

        eventProducer.sendOrderCreatedEvent(event);

        CreateOrderResponse response = new CreateOrderResponse(
                orderId,
                "CREATED",
                "Order created successfully and sent to processing"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getStatus(@PathVariable String id) {
        String status = orderStatusStore.getStatus(id);
        return ResponseEntity.ok("Status for order " + id + ": " + status);
    }

}
