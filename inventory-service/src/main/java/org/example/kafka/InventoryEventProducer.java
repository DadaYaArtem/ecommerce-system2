package org.example.kafka;

import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReservedEvent(InventoryReservedEvent event) {
        kafkaTemplate.send("inventory-events", event);
        System.out.println("📤 Sent: " + event);
    }

    public void sendNotAvailableEvent(InventoryNotAvailableEvent event) {
        kafkaTemplate.send("inventory-events", event);
        System.out.println("📤 Sent: " + event);
    }
}
