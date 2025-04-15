package org.example.kafka;

import org.example.events.InventoryNotAvailableEvent;
import org.example.events.InventoryReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_EVENTS;


@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReservedEvent(InventoryReservedEvent event) {
        kafkaTemplate.send(INVENTORY_EVENTS, event);
        System.out.println("📤 Sent: " + event);
    }

    public void sendNotAvailableEvent(InventoryNotAvailableEvent event) {
        kafkaTemplate.send(INVENTORY_EVENTS, event);
        System.out.println("📤 Sent: " + event);
    }
}
