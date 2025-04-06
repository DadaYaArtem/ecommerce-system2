package org.example.listener;

import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.example.event.OrderCreatedEvent;
import org.example.kafka.InventoryEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    @Autowired
    private InventoryEventProducer producer;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void listen(OrderCreatedEvent event) {
        System.out.println("📦 Inventory received: " + event);

        if (event.getQuantity() <= 5) {
            producer.sendReservedEvent(new InventoryReservedEvent(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity()
            ));
        } else {
            producer.sendNotAvailableEvent(new InventoryNotAvailableEvent(
                    event.getOrderId(),
                    event.getProductId()
            ));
        }
    }
}
