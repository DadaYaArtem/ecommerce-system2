package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.saga.OrderSagaManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_EVENTS;
import static org.example.kafka.constants.KafkaGroups.ORDER_SERVICE;


@Component
public class PaymentEventListener {

    private final OrderSagaManager sagaManager;

    public PaymentEventListener(OrderSagaManager sagaManager) {
        this.sagaManager = sagaManager;
    }

    @KafkaListener(topics = PAYMENT_EVENTS, groupId = ORDER_SERVICE)
    public void listenPaymentEvents(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Order-service отримав подію: " + event);
        sagaManager.handlePaymentEvent(event);
    }
}
