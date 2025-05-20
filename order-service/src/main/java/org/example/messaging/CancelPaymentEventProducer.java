package org.example.messaging;

import org.example.events.CancelPaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_CANCELLATIONS;

/**
 * Producer for payment cancellation events.
 * Sends events to the payment service to cancel pending payment requests.
 */
@Component
public class CancelPaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CancelPaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a payment cancellation event to the payment service.
     * 
     * @param event The cancellation event to send
     */
    public void sendCancelPayment(CancelPaymentEvent event) {
        System.out.println("📤 Sending CancelPaymentEvent: " + event);
        kafkaTemplate.send(PAYMENT_CANCELLATIONS, event);
    }
}