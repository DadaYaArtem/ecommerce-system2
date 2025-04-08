package org.example.listener;

import org.example.event.PaymentRequestEvent;
import org.example.event.PriceResponseEvent;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.model.OrderInfo;
import org.example.store.OrderInfoStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PriceResponseListener {

    private final PaymentRequestEventProducer paymentProducer;
    private final OrderInfoStore infoStore;

    public PriceResponseListener(PaymentRequestEventProducer paymentProducer,
                                 OrderInfoStore infoStore) {
        this.paymentProducer = paymentProducer;
        this.infoStore = infoStore;
    }

    @KafkaListener(topics = "price-responses", groupId = "order-group")
    public void listen(PriceResponseEvent event) {
        System.out.println("📥 Отримано PriceResponseEvent: " + event);

        OrderInfo info = infoStore.get(event.getOrderId());

        if (info == null) {
            System.out.println("⚠️ OrderInfo не знайдено для orderId: " + event.getOrderId());
            return;
        }

        paymentProducer.sendPaymentRequest(new PaymentRequestEvent(
                event.getOrderId(),
                event.getProductId(),
                info.getQuantity(),
                info.getCustomerId(),
                event.getPrice()
        ));
    }
}
