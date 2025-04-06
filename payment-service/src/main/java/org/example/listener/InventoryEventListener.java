package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.event.PriceRequestEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }



    @KafkaListener(topics = "inventory-events", groupId = "payment-group")
    public void handleInventoryEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Payment-service отримав подію: " + event);

        if (event instanceof InventoryReservedEvent reserved) {
            PriceRequestEvent request = new PriceRequestEvent(
                    reserved.getOrderId(),
                    reserved.getProductId()
            );
            kafkaTemplate.send("price-requests", request);
            System.out.println("📤 Запит ціни на продукт: " + request);
        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            PaymentFailedEvent failed = new PaymentFailedEvent(
                    notAvailable.getOrderId(),
                    "Inventory not available for product " + notAvailable.getProductId()
            );
            kafkaTemplate.send("payment-events", failed);
            System.out.println("❌ Відправлено PaymentFailedEvent: " + failed);
        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }


}
