package org.example.messaging;

import org.example.events.ReleaseInventoryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaTopics.INVENTORY_RELEASES;

@Component
public class ReleaseInventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReleaseInventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ReleaseInventoryEvent event) {
        kafkaTemplate.send(INVENTORY_RELEASES, event);
        System.out.println("📤 Надіслано ReleaseInventoryEvent: " + event);
    }
}