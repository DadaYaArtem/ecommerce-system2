package org.example.saga;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.event.*;
import org.example.messaging.PriceRequestProducer;
import org.example.store.OrderInfoStore;
import org.example.messaging.PaymentRequestEventProducer;
import org.example.store.OrderStatusStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaManager {

    private final OrderStatusStore statusStore;
    private final OrderInfoStore infoStore;
    private final PaymentRequestEventProducer paymentProducer;
    private final PriceRequestProducer priceRequestProducer;

    public OrderSagaManager(OrderStatusStore statusStore, OrderInfoStore infoStore, PaymentRequestEventProducer paymentProducer, PriceRequestProducer priceRequestProducer) {
        this.statusStore = statusStore;
        this.infoStore = infoStore;
        this.paymentProducer = paymentProducer;
        this.priceRequestProducer = priceRequestProducer;
    }

    public void handleInventoryEvent(Object event) {
        if (event instanceof InventoryReservedEvent reserved) {
            System.out.println("✅ Saga: Inventory reserved for " + reserved.getOrderId());
            statusStore.setStatus(reserved.getOrderId(), "AWAITING_PRICE");

            // отправляем запрос на цену в product-service
            priceRequestProducer.sendPriceRequest(new PriceRequestEvent(
                    reserved.getOrderId(),
                    reserved.getProductId()
            ));

        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            System.out.println("❌ Saga: Inventory not available for " + notAvailable.getOrderId());
            statusStore.setStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");
        }
    }


    public void handlePaymentEvent(Object event) {
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
