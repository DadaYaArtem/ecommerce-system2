package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.ReleaseInventoryEvent;
import org.example.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.INVENTORY_SERVICE;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_RELEASES;


@Component
public class ReleaseInventoryListener {

    private final InventoryService inventoryService;

    public ReleaseInventoryListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = INVENTORY_RELEASES, groupId = INVENTORY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof ReleaseInventoryEvent event) {
            System.out.println("🔁 Отримано запит на скасування резерву: " + event);

            inventoryService.releaseReservation(event.getOrderId());

            System.out.println("✅ Резерв успішно знято для замовлення: " + event.getOrderId());
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}
