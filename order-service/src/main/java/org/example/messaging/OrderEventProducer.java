package org.example.messaging;

import org.example.events.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.ORDER_EVENTS;


@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_EVENTS, event);
        System.out.println("📤 Надіслано OrderCreatedEvent: " + event);
    }
}
