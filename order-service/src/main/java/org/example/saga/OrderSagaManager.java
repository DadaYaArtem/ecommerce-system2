package org.example.saga;

import org.example.model.Order;
import org.example.events.*;
import org.example.messaging.PriceRequestProducer;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.service.OrderDbService;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaManager {

    private final OrderDbService orderDbService;
    private final PriceRequestProducer priceRequestProducer;
    private final ReleaseInventoryEventProducer releaseInventoryProducer;

    public OrderSagaManager(OrderDbService orderDbService,
                            PriceRequestProducer priceRequestProducer,
                            ReleaseInventoryEventProducer releaseInventoryProducer) {
        this.orderDbService = orderDbService;
        this.priceRequestProducer = priceRequestProducer;
        this.releaseInventoryProducer = releaseInventoryProducer;
    }

    public void handleInventoryEvent(Object event) {
        if (event instanceof InventoryReservedEvent reserved) {
            System.out.println("✅ Saga: Inventory reserved for " + reserved.getOrderId());

            orderDbService.updateItemStatus(reserved.getOrderId(), reserved.getProductId(), "RESERVED");

            tryStartPricingIfReady(reserved.getOrderId());

        } else if (event instanceof InventoryNotAvailableEvent notAvailable) {
            System.out.println("❌ Saga: Inventory not available for " + notAvailable.getOrderId());

            orderDbService.updateItemStatus(notAvailable.getOrderId(), notAvailable.getProductId(), "FAILED_RESERVE");

            // ✅ сразу отменяем весь заказ
            orderDbService.updateStatus(notAvailable.getOrderId(), "FAILED_INVENTORY");

            System.out.println("❌ Статус FAILED_INVENTORY: деякі товари недоступні");
        }
    }

    public void handlePaymentEvent(Object event) {
        if (event instanceof PaymentConfirmedEvent confirmed) {
            System.out.println("✅ Оплата успішна для замовлення: " + confirmed.getOrderId());

            orderDbService.updateStatus(confirmed.getOrderId(), "PAID");

        } else if (event instanceof PaymentFailedEvent failed) {
            System.out.println("❌ Оплата неуспішна: " + failed.getOrderId());

            orderDbService.updateStatus(failed.getOrderId(), "FAILED_PAYMENT");

            // Компенсация — отпускаем все зарезервированные товары
            orderDbService.findById(failed.getOrderId()).ifPresent(order -> {
                order.getItems().stream()
                        .filter(item -> "RESERVED".equals(item.getStatus()))
                        .forEach(item -> releaseInventoryProducer.send(new ReleaseInventoryEvent(
                                order.getId(),
                                item.getProductId(),
                                item.getQuantity()
                        )));
            });
        }
    }

    public void tryStartPricingIfReady(String orderId) {
        if (orderDbService.allItemsReserved(orderId)) {
            System.out.println("✅ Всі товари зарезервовані. Надсилаємо запити цін.");

            orderDbService.findById(orderId).ifPresent(order -> {
                order.getItems().forEach(item -> {
                    priceRequestProducer.sendPriceRequest(new PriceRequestEvent(
                            order.getId(),
                            item.getProductId()
                    ));
                    System.out.println("📤 Надіслано PriceRequestEvent: " + item.getProductId());
                });
            });
        }
    }
}
