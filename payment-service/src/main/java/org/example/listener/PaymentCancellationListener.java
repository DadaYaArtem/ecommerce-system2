package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.CancelPaymentEvent;
import org.example.gateway.PaymentStatus;
import org.example.model.Payment;
import org.example.service.PaymentDbService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.example.kafka.constants.KafkaGroups.PAYMENT_SERVICE;
import static org.example.kafka.constants.KafkaTopics.PAYMENT_CANCELLATIONS;

@Component
public class PaymentCancellationListener {

    private final PaymentDbService paymentDbService;

    public PaymentCancellationListener(PaymentDbService paymentDbService) {
        this.paymentDbService = paymentDbService;
    }

    @KafkaListener(topics = PAYMENT_CANCELLATIONS, groupId = PAYMENT_SERVICE,
            containerFactory = "paymentKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof CancelPaymentEvent event) {
            System.out.println("📥 Отримано CancelPaymentEvent: " + event);

            Payment payment = paymentDbService.findByOrderId(event.getOrderId());
            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentDbService.save(payment);
                System.out.println("🚫 Оплату для замовлення " + event.getOrderId() +
                        " скасовано");
            } else {
                System.out.println("⚠️ Не знайдено оплату для замовлення " +
                        event.getOrderId());
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}
