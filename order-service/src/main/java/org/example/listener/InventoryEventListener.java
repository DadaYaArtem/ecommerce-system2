package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.saga.OrderSagaManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.INVENTORY_EVENTS;
import static org.example.kafka.constants.KafkaGroups.ORDER_SERVICE;


@Component
public class InventoryEventListener {

    private final OrderSagaManager sagaManager;

    public InventoryEventListener(OrderSagaManager sagaManager) {
        this.sagaManager = sagaManager;
    }

    @KafkaListener(topics = INVENTORY_EVENTS, groupId = ORDER_SERVICE)
    public void listenInventory(ConsumerRecord<String, Object> record) {
        sagaManager.handleInventoryEvent(record.value());
    }
}
