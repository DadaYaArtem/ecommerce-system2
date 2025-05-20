package org.example.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.PaymentRequestEvent;
import org.example.events.PriceResponseEvent;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.service.OrderDbService;
import org.example.service.TraceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.example.kafka.constants.KafkaTopics.PRICE_RESPONSES;
import static org.example.kafka.constants.KafkaGroups.ORDER_SERVICE;


@Component
public class PriceResponseListener {

    private final PaymentRequestEventProducer paymentProducer;
    private final OrderDbService orderDbService;
    private final TraceService traceService;

    public PriceResponseListener(PaymentRequestEventProducer paymentProducer,
                                 OrderDbService orderDbService, TraceService traceService) {
        this.paymentProducer = paymentProducer;
        this.orderDbService = orderDbService;
        this.traceService = traceService;
    }

    @KafkaListener(topics = PRICE_RESPONSES, groupId = ORDER_SERVICE)
    public void listen(ConsumerRecord<String, Object> record) {
        traceService.traceOperation("processPriceResponse", () -> {
            Object raw = record.value();

            if (raw instanceof PriceResponseEvent event) {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("order.id", event.getOrderId());
                attributes.put("product.id", event.getProductId());
                attributes.put("price", String.valueOf(event.getPrice()));
                traceService.addSpanAttributes(attributes);

                System.out.println("📥 Отримано PriceResponseEvent: " + event);

                orderDbService.updateItemPrice(event.getOrderId(), event.getProductId(), event.getPrice());

                if (orderDbService.allItemsPriced(event.getOrderId())) {
                    double total = orderDbService.calculateTotalAmount(event.getOrderId());

                    orderDbService.findById(event.getOrderId()).ifPresent(order -> {
                        // Update order status to reflect we're waiting for payment
                        orderDbService.updateStatus(order.getId(), "AWAITING_PAYMENT");

                        PaymentRequestEvent paymentEvent = new PaymentRequestEvent(
                                order.getId(),
                                total,
                                order.getCustomerId()
                        );

                        paymentProducer.sendPaymentRequest(paymentEvent);

                        traceService.addSpanEvent("Payment request sent",
                                Map.of("total_amount", String.valueOf(total)));

                        System.out.println("📤 Надсилаємо загальний PaymentRequestEvent: " + total);
                    });
                }
            } else {
                traceService.addSpanEvent("Unknown event type received",
                        Map.of("event_class", raw.getClass().getName()));
                System.out.println("⚠️ Невідомий тип події: " + raw.getClass().getName());
            }

            return null;
        });
    }
}