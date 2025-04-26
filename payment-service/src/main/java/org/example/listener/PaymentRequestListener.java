package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.PaymentConfirmedEvent;
import org.example.events.PaymentFailedEvent;
import org.example.events.PaymentRequestEvent;
import org.example.gateway.PaymentGateway;
import org.example.gateway.PaymentRequest;
import org.example.gateway.PaymentResult;
import org.example.gateway.PaymentStatus;
import org.example.messaging.PaymentEventProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.PAYMENT_SERVICE;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_REQUESTS;

@Component
public class PaymentRequestListener {

    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer producer;

    public PaymentRequestListener(PaymentGateway paymentGateway, PaymentEventProducer producer) {
        this.paymentGateway = paymentGateway;
        this.producer = producer;
    }

    @KafkaListener(topics = PAYMENT_REQUESTS, groupId = PAYMENT_SERVICE, containerFactory = "paymentKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof PaymentRequestEvent event) {
            System.out.println("📥 Отримано PaymentRequestEvent: " + event);

            if (event.getCustomerId().startsWith("9")) {
                System.out.println("DDDDDDLLLLLLLLLQQQQQQQQQ");
                throw new RuntimeException("💥 Тестова помилка у платіжному сервісі");
            }

            PaymentResult result = paymentGateway.processPayment(
                    new PaymentRequest(event.getOrderId(), event.getCustomerId(), event.getTotalAmount())
            );

            if (result.getStatus() == PaymentStatus.SUCCESSFUL) {
                producer.sendPaymentConfirmedEvent(new PaymentConfirmedEvent(
                        event.getOrderId(),
                        event.getTotalAmount()
                ));
                System.out.println("✅ Оплата успішна. Надіслано PaymentConfirmedEvent");
            } else {
                producer.sendPaymentFailedEvent(new PaymentFailedEvent(
                        event.getOrderId(),
                        event.getTotalAmount()
                ));
                System.out.println("❌ Оплата відхилена. Надіслано PaymentFailedEvent");
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}
