package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.PaymentRequestEvent;
import org.example.events.PaymentConfirmedEvent;
import org.example.events.PaymentFailedEvent;
import org.example.messaging.PaymentEventProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaTopics.PAYMENT_REQUESTS;
import static org.example.kafka.constants.KafkaGroups.PAYMENT_SERVICE;

@Component
public class PaymentRequestListener {

    private final PaymentEventProducer producer;

    public PaymentRequestListener(PaymentEventProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = PAYMENT_REQUESTS, groupId = PAYMENT_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof PaymentRequestEvent event) {
            System.out.println("📥 Отримано PaymentRequestEvent: " + event);

            double total = event.getQuantity() * event.getPrice();

            if (total > 10000.0) {
                producer.sendPaymentFailedEvent(new PaymentFailedEvent(
                        event.getOrderId(),
                        "💸 Сума перевищує ліміт 10000. Total: " + total
                ));
            } else {
                producer.sendPaymentConfirmedEvent(new PaymentConfirmedEvent(
                        event.getOrderId(),
                        total
                ));
            }

        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}
