package org.example;

import org.example.events.PaymentConfirmedEvent;
import org.example.events.PaymentFailedEvent;
import org.example.events.ReleaseInventoryEvent;
import org.example.messaging.ReleaseInventoryEventProducer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.saga.OrderSagaManager;
import org.example.service.OrderDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaManagerTest {

    @Mock
    private OrderDbService orderDbService;

    @Mock
    private ReleaseInventoryEventProducer releaseInventoryProducer;

    @InjectMocks
    private OrderSagaManager sagaManager;

    @Test
    void shouldUpdateStatusToPaidOnPaymentConfirmed() {
        // given
        PaymentConfirmedEvent event = new PaymentConfirmedEvent("order-123", 100.0);

        // when
        sagaManager.handlePaymentEvent(event);

        // then
        verify(orderDbService).updateStatus("order-123", "PAID");
        verifyNoInteractions(releaseInventoryProducer);
    }

    @Test
    void shouldReleaseInventoryOnPaymentFailed() {
        PaymentFailedEvent event = new PaymentFailedEvent("order-123", 100.0);

        Order order = new Order();
        order.setId("order-123");

        OrderItem item = new OrderItem();
        item.setProductId("prod1");
        item.setQuantity(2);
        item.setStatus("RESERVED");

        order.getItems().add(item);

        when(orderDbService.findById("order-123")).thenReturn(Optional.of(order));

        sagaManager.handlePaymentEvent(event);

        verify(orderDbService).updateStatus("order-123", "FAILED_PAYMENT");

        ArgumentCaptor<ReleaseInventoryEvent> captor = ArgumentCaptor.forClass(ReleaseInventoryEvent.class);
        verify(releaseInventoryProducer).send(captor.capture());

        ReleaseInventoryEvent actual = captor.getValue();
        assertEquals("order-123", actual.getOrderId());
        assertEquals("prod1", actual.getProductId());
        assertEquals(2, actual.getQuantity());
    }
}
