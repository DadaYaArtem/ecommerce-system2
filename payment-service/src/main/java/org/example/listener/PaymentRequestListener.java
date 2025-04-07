package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.PaymentRequestEvent;
import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.messaging.PaymentEventProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentRequestListener {

    private final PaymentEventProducer producer;

    // для симуляції балансу клієнтів
    private final Map<String, Double> balances = new HashMap<>();

    public PaymentRequestListener(PaymentEventProducer producer) {
        this.producer = producer;

        // додай тестовий баланс
        balances.put("c1", 10000.0);
    }

    @KafkaListener(topics = "payment-requests", groupId = "payment-group")
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof PaymentRequestEvent event) {
            System.out.println("📥 Payment-service отримав подію: " + event);

            double pricePerUnit = getPriceForProduct(event.getProductId());
            double total = event.getQuantity() * pricePerUnit;

            double customerBalance = balances.getOrDefault(event.getCustomerId(), 0.0);

            if (customerBalance >= total) {
                balances.put(event.getCustomerId(), customerBalance - total);

                producer.sendPaymentConfirmedEvent(new PaymentConfirmedEvent(
                        event.getOrderId(),
                        total
                ));

            } else {
                producer.sendPaymentFailedEvent(new PaymentFailedEvent(
                        event.getOrderId(),
                        "💸 Недостатньо коштів для " + total
                ));
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }

    private double getPriceForProduct(String productId) {
        return switch (productId) {
            case "p123" -> 9999.0;
            case "p456" -> 1999.0;
            case "p789" -> 9999.0;
            default -> 5000.0;
        };
    }
}
