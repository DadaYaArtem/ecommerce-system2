package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.PaymentConfirmedEvent;
import org.example.events.PaymentFailedEvent;
import org.example.model.DeliveryStatus;
import org.example.service.DeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.DELIVERY_SERVICE;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_EVENTS;

@Component
public class DeliveryEventListener {

    private final DeliveryService deliveryService;

    public DeliveryEventListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = PAYMENT_EVENTS, groupId = DELIVERY_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        if (event instanceof PaymentConfirmedEvent confirmed) {
            System.out.println("📥 Delivery-service отримав PaymentConfirmedEvent: " + event);
            deliveryService.createDelivery(confirmed.getOrderId(), confirmed.getTotalAmount());
        } else if (event instanceof PaymentFailedEvent failed) {
            System.out.println("📥 Delivery-service отримав PaymentFailedEvent: " + event);
            deliveryService.updateStatus(failed.getOrderId(), DeliveryStatus.CANCELLED);
        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }
}
