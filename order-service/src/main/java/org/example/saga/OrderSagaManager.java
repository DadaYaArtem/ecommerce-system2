package org.example.saga;

import org.example.events.*;
import org.example.messaging.PriceRequestProducer;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.store.OrderInfoStore;
import org.example.store.OrderStatusStore;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaManager {

    private final OrderStatusStore statusStore;
    private final PriceRequestProducer priceRequestProducer;
    private final OrderInfoStore infoStore;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;

    public OrderSagaManager(OrderStatusStore statusStore, PriceRequestProducer priceRequestProducer, OrderInfoStore orderInfoStore, ReleaseInventoryEventProducer releaseInventoryEventProducer) {
        this.statusStore = statusStore;
        this.priceRequestProducer = priceRequestProducer;
        this.infoStore = orderInfoStore;
        this.releaseInventoryProducer = releaseInventoryEventProducer;
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
            statusStore.setStatus(failed.getOrderId(), "FAILED_PAYMENT");

            var info = infoStore.get(failed.getOrderId());
            if (info != null) {
                releaseInventoryProducer.send(new ReleaseInventoryEvent(
                        failed.getOrderId(),
                        info.getProductId(),
                        info.getQuantity()
                ));
            }
        } else {
            System.out.println("⚠️ Невідомий тип події: " + event.getClass().getName());
        }
    }

}
