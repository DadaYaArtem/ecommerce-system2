package org.example.listener;

import org.example.events.PaymentRequestEvent;
import org.example.events.PriceResponseEvent;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.model.OrderInfo;
import org.example.store.OrderInfoStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PRICE_RESPONSES;
import static org.example.kafka.constants.KafkaGroups.ORDER_SERVICE;


@Component
public class PriceResponseListener {

    private final PaymentRequestEventProducer paymentProducer;
    private final OrderInfoStore infoStore;

    public PriceResponseListener(PaymentRequestEventProducer paymentProducer,
                                 OrderInfoStore infoStore) {
        this.paymentProducer = paymentProducer;
        this.infoStore = infoStore;
    }

    @KafkaListener(topics = PRICE_RESPONSES, groupId = ORDER_SERVICE)
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
