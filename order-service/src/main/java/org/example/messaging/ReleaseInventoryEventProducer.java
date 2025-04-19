package org.example.messaging;

import org.example.events.ReleaseInventoryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReleaseInventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReleaseInventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ReleaseInventoryEvent event) {
        kafkaTemplate.send("inventory-events", event);
        System.out.println("📤 Надіслано ReleaseInventoryEvent: " + event);
    }
}