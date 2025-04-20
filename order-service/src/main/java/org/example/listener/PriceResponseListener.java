package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.PaymentRequestEvent;
import org.example.events.PriceResponseEvent;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.service.OrderDbService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PRICE_RESPONSES;
import static org.example.kafka.constants.KafkaGroups.ORDER_SERVICE;


@Component
public class PriceResponseListener {

    private final PaymentRequestEventProducer paymentProducer;
    private final OrderDbService orderDbService;

    public PriceResponseListener(PaymentRequestEventProducer paymentProducer,
                                 OrderDbService orderDbService) {
        this.paymentProducer = paymentProducer;
        this.orderDbService = orderDbService;
    }

    @KafkaListener(topics = PRICE_RESPONSES, groupId = ORDER_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        Object raw = record.value();

        if (raw instanceof PriceResponseEvent event) {
            System.out.println("📥 Отримано PriceResponseEvent: " + event);

            orderDbService.updateItemPrice(event.getOrderId(), event.getProductId(), event.getPrice());

            if (orderDbService.allItemsPriced(event.getOrderId())) {
                double total = orderDbService.calculateTotalAmount(event.getOrderId());

                orderDbService.findById(event.getOrderId()).ifPresent(order -> {
                    paymentProducer.sendPaymentRequest(new PaymentRequestEvent(
                            order.getId(),
                            total,
                            order.getCustomerId()
                    ));
                    System.out.println("📤 Надсилаємо загальний PaymentRequestEvent: " + total);
                });
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
        }
    }
}