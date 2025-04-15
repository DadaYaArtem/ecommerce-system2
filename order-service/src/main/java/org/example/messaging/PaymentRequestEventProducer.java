package org.example.messaging;

import org.example.event.PaymentRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_REQUESTS;


@Component
public class PaymentRequestEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentRequestEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentRequest(PaymentRequestEvent event) {
        System.out.println("📤 Надсилаємо PaymentRequestEvent: " + event);
        kafkaTemplate.send(PAYMENT_REQUESTS, event);
    }
}
