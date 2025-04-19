package org.example.listener;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.InventoryReservedEvent;
import org.example.events.ReleaseInventoryEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.INVENTORY_SERVICE;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_EVENTS;

@Component
public class CompensatoryListener {
    @KafkaListener(topics = INVENTORY_EVENTS, groupId = INVENTORY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();
        if (raw instanceof ReleaseInventoryEvent event) {
            System.out.println("🔁 Отримано запит на скасування резерву: " + event);
//        } else if (raw instanceof InventoryReservedEvent) {
//            // не обрабатываем в этом Listener-е
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }

    }
}
