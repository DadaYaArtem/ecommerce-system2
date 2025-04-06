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
    private final OrderInfoStore orderInfoStore;


    public PriceResponseListener(KafkaTemplate<String, Object> kafkaTemplate, OrderInfoStore orderInfoStore) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderInfoStore = orderInfoStore;
    }

    @KafkaListener(topics = "price-responses", groupId = "payment-group")
    public void handlePriceResponse(PriceResponseEvent event) {
        System.out.println("💵 Отримано ціну: " + event);

        int quantity = orderInfoStore.getQuantity(event.getOrderId());

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
