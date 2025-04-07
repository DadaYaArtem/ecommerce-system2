package org.example.messaging;

import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentConfirmedEvent(PaymentConfirmedEvent event) {
        System.out.println("✅ Відправлено PaymentConfirmedEvent: " + event);
        kafkaTemplate.send("payment-events", event);
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        System.out.println("❌ Відправлено PaymentFailedEvent: " + event);
        kafkaTemplate.send("payment-events", event);
    }
}
