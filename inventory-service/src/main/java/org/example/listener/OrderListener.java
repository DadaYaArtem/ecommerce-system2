package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.InventoryNotAvailableEvent;
import org.example.events.InventoryReservedEvent;
import org.example.events.OrderCreatedEvent;
import org.example.kafka.InventoryEventProducer;
import org.example.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.ORDER_EVENTS;
import static org.example.kafka.constants.KafkaGroups.INVENTORY_SERVICE;

@Component
public class OrderListener {

    private final InventoryService inventoryService;
    private final InventoryEventProducer eventProducer;

    public OrderListener(InventoryService inventoryService, InventoryEventProducer producer) {
        this.inventoryService = inventoryService;
        this.eventProducer = producer;
    }

    @KafkaListener(topics = ORDER_EVENTS, groupId = INVENTORY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof OrderCreatedEvent event) {
            System.out.println("📦 Inventory received: " + event);

            boolean reserved = inventoryService.tryReserve(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity()
            );

            if (reserved) {
                InventoryReservedEvent event1 = new InventoryReservedEvent(
                        event.getOrderId(),
                        event.getProductId(),
                        event.getQuantity()
                );
                eventProducer.sendReservedEvent(event1);
            } else {
                InventoryNotAvailableEvent event1 = new InventoryNotAvailableEvent(
                        event.getOrderId(),
                        event.getProductId()
                );
                eventProducer.sendNotAvailableEvent(event1);
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }

}
