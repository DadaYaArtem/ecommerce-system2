package org.example.controller;

import org.example.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderController(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public String createOrder(@RequestParam String productId, @RequestParam int quantity) {
        String orderId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, productId, quantity);
        kafkaTemplate.send("order-events", event);
        return "🟢 Order sent with ID: " + orderId;
    }
}
