package org.example.listener;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.InventoryReservedEvent;
import org.example.events.ReleaseInventoryEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.INVENTORY_SERVICE;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_EVENTS;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_RELEASES;

@Component
public class CompensatoryListener {
    @KafkaListener(topics = INVENTORY_RELEASES, groupId = INVENTORY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();
        if (raw instanceof ReleaseInventoryEvent event) {
            System.out.println("🔁 Отримано запит на скасування резерву: " + event);
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }

    }
}
