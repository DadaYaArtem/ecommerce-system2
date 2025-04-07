package org.example.saga;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.PaymentRequestEvent;
import org.example.store.OrderInfoStore;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.event.InventoryNotAvailableEvent;
import org.example.event.InventoryReservedEvent;
import org.example.event.PaymentConfirmedEvent;
import org.example.event.PaymentFailedEvent;
import org.example.store.OrderStatusStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaManager {

    private final OrderStatusStore statusStore;
    private final OrderInfoStore infoStore;
    private final PaymentRequestEventProducer paymentProducer;

    public OrderSagaManager(OrderStatusStore statusStore, OrderInfoStore infoStore, PaymentRequestEventProducer paymentProducer) {
        this.statusStore = statusStore;
        this.infoStore = infoStore;
        this.paymentProducer = paymentProducer;
    }

    public void handleInventoryEvent(Object event) {
        if (event instanceof InventoryReservedEvent reserved) {
            System.out.println("✅ Saga: Inventory reserved for " + reserved.getOrderId());
            statusStore.setStatus(reserved.getOrderId(), "AWAITING_CONFIRMATION");

            var info = infoStore.get(reserved.getOrderId());

            paymentProducer.sendPaymentRequest(new PaymentRequestEvent(
                    reserved.getOrderId(),
                    reserved.getProductId(),
                    reserved.getQuantity(),
                    info.getCustomerId()
            ));

        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            System.out.println("❌ Saga: Inventory not available for " + notAvailable.getOrderId());
            statusStore.setStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvents(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        System.out.println("📥 Order-service отримав подію: " + event);

        if (event instanceof PaymentConfirmedEvent confirmed) {
            System.out.println("✅ Оплата успішна для замовлення: " + confirmed.getOrderId());
            statusStore.setStatus(confirmed.getOrderId(), "PAID");

        } else if (event instanceof PaymentFailedEvent failed) {
            System.out.println("❌ Оплата неуспішна: " + failed);
            statusStore.setStatus(failed.getOrderId(), "FAILED_PAYMENT");

        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }
}
