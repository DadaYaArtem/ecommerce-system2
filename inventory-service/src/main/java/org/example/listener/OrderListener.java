package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.InventoryNotAvailableEvent;
import org.example.events.InventoryReservedEvent;
import org.example.events.OrderCreatedEvent;
import org.example.events.ReleaseInventoryEvent;
import org.example.kafka.InventoryEventProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.ORDER_EVENTS;
import static org.example.kafka.constants.KafkaGroups.INVENTORY_SERVICE;

@Component
public class OrderListener {

    private final InventoryEventProducer producer;

    public OrderListener(InventoryEventProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = ORDER_EVENTS, groupId = INVENTORY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof OrderCreatedEvent event) {
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
        }else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}
