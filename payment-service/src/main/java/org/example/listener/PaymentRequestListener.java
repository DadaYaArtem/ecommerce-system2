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
import org.example.model.Payment;
import org.example.service.PaymentDbService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.example.kafka.constants.KafkaGroups.PAYMENT_SERVICE;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_REQUESTS;

@Component
public class PaymentRequestListener {

    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer producer;
    private final PaymentDbService paymentDbService;

    public PaymentRequestListener(PaymentGateway paymentGateway,
                                  PaymentEventProducer producer,
                                  PaymentDbService paymentDbService) {
        this.paymentGateway = paymentGateway;
        this.producer = producer;
        this.paymentDbService = paymentDbService;
    }

    @KafkaListener(topics = PAYMENT_REQUESTS, groupId = PAYMENT_SERVICE, containerFactory = "paymentKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof PaymentRequestEvent event) {
            System.out.println("📥 Отримано PaymentRequestEvent: " + event);

            if (event.getCustomerId().startsWith("9")) {
                throw new RuntimeException("💥 Тестова помилка у платіжному сервісі");
            }

            Payment payment = new Payment();
            payment.setOrderId(event.getOrderId());
            payment.setAmount(event.getTotalAmount());
            payment.setStatus(PaymentStatus.PENDING);
            paymentDbService.create(payment);

            PaymentResult result = paymentGateway.processPayment(
                    new PaymentRequest(event.getOrderId(), event.getCustomerId(), event.getTotalAmount())
            );

            paymentDbService.updateStatus(payment.getId(), result.getStatus(),
                    result.getTransactionId(), result.getErrorMessage());

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
