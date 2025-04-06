package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.example.store.OrderStatusStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final OrderStatusStore statusStore;

    public InventoryEventListener(OrderStatusStore statusStore) {
        this.statusStore = statusStore;
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void handleInventoryEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Order-service отримав подію: " + event);

        if (event instanceof InventoryReservedEvent reserved) {
            System.out.println("✅ Inventory reserved for order: " + reserved.getOrderId());
            statusStore.setStatus(reserved.getOrderId(), "AWAITING_CONFIRMATION");
        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            System.out.println("❌ Inventory not available for order: " + notAvailable.getOrderId());
            statusStore.setStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");
        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }
}
