package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.store.OrderStatusStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final OrderStatusStore statusStore;

    public PaymentEventListener(OrderStatusStore statusStore) {
        this.statusStore = statusStore;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Order-service отримав подію (оплата): " + event);

        if (event instanceof PaymentConfirmedEvent confirmed) {
            System.out.println("✅ Оплата підтверджена: " + confirmed.getOrderId());
            statusStore.setStatus(confirmed.getOrderId(), "PAID");
        } else if (event instanceof PaymentFailedEvent failed) {
            System.out.println("❌ Оплата неуспішна: " + failed.getOrderId());
            statusStore.setStatus(failed.getOrderId(), "FAILED_PAYMENT");
        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }
}
