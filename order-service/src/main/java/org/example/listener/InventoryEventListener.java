package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.example.saga.OrderSagaManager;
import org.example.store.OrderStatusStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final OrderSagaManager sagaManager;

    public InventoryEventListener(OrderSagaManager sagaManager) {
        this.sagaManager = sagaManager;
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void listenInventory(ConsumerRecord<String, Object> record) {
        sagaManager.handleInventoryEvent(record.value());
    }
}
