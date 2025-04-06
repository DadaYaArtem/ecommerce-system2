package org.example.listener;

import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.event.PriceResponseEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PriceResponseListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PriceResponseListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "price-responses", groupId = "payment-group")
    public void handlePriceResponse(PriceResponseEvent event) {
        System.out.println("💵 Отримано ціну: " + event);

        // 🔢 Симулюємо кількість (можна зберігати десь)
        int quantity = 1; // тимчасово фіксовано або підключимо до сховища

        double total = event.getPrice() * quantity;

        if (total <= 9000) {
            PaymentConfirmedEvent confirmed = new PaymentConfirmedEvent(event.getOrderId(), total);
            kafkaTemplate.send("payment-events", confirmed);
            System.out.println("✅ Оплата підтверджена: " + confirmed);
        } else {
            PaymentFailedEvent failed = new PaymentFailedEvent(event.getOrderId(), "💸 Недостатньо коштів для " + total);
            kafkaTemplate.send("payment-events", failed);
            System.out.println("❌ Оплата неуспішна: " + failed);
        }
    }
}
