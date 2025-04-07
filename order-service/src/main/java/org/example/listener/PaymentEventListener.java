package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.saga.OrderSagaManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final OrderSagaManager sagaManager;

    public PaymentEventListener(OrderSagaManager sagaManager) {
        this.sagaManager = sagaManager;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void listenPaymentEvents(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Order-service отримав подію: " + event);
        sagaManager.handlePaymentEvent(event);
    }
}
