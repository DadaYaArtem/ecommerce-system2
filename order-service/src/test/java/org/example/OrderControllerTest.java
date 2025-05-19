// OrderControllerTest.java
package org.example;

import org.example.controller.OrderController;
import org.example.dto.CreateOrderRequest;
import org.example.dto.CreateOrderResponse;
import org.example.events.OrderCreatedEvent;
import org.example.messaging.OrderEventProducer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.service.MetricsService;
import org.example.service.OrderDbService;
import org.example.service.TraceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderDbService orderDbService;

    @Mock
    private OrderEventProducer orderProducer;

    @Mock
    private TraceService traceService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private OrderController orderController;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> eventCaptor;

    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        // Setup a sample order request
        request = new CreateOrderRequest() {
            @Override
            public String getCustomerId() {
                return "customer123";
            }

            @Override
            public List<ItemRequest> getItems() {
                List<ItemRequest> items = new ArrayList<>();
                items.add(new ItemRequest() {
                    @Override
                    public String getProductId() {
                        return "product123";
                    }

                    @Override
                    public int getQuantity() {
                        return 2;
                    }
                });
                return items;
            }
        };

        // Setup metric service to pass through the supplier function
        when(metricsService.recordTimedOperation(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        // Setup trace service to pass through the supplier function
        when(traceService.traceOperation(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
    }


    @Test
    void createOrder_ShouldReturnSuccessResponse() {
        // Arrange
        doNothing().when(orderDbService).save(any(Order.class));
        doNothing().when(orderProducer).sendOrderCreatedEvent(any(OrderCreatedEvent.class));
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());
        doNothing().when(metricsService).incrementOrderCreated();

        // Act
        ResponseEntity<CreateOrderResponse> response = orderController.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CREATED", response.getBody().getStatus());
        assertEquals("Order created successfully", response.getBody().getMessage());

        // Verify order was saved to database
        verify(orderDbService).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals("customer123", savedOrder.getCustomerId());
        assertEquals("CREATED", savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
        OrderItem savedItem = savedOrder.getItems().get(0);
        assertEquals("product123", savedItem.getProductId());
        assertEquals(2, savedItem.getQuantity());

        // Verify producer sent event
        verify(orderProducer).sendOrderCreatedEvent(eventCaptor.capture());
        OrderCreatedEvent sentEvent = eventCaptor.getValue();
        assertEquals(savedOrder.getId(), sentEvent.getOrderId());
        assertEquals("product123", sentEvent.getProductId());
        assertEquals(2, sentEvent.getQuantity());
        assertEquals("customer123", sentEvent.getCustomerId());

        // Verify metrics
        verify(metricsService).incrementOrderCreated();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        String orderId = "order123";
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("CREATED");
        mockOrder.setCustomerId("customer123");

        when(orderDbService.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());

        ResponseEntity<?> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof CreateOrderResponse);
        CreateOrderResponse orderResponse = (CreateOrderResponse) response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals("CREATED", orderResponse.getStatus());
        assertEquals("Order status fetched successfully", orderResponse.getMessage());

        verify(traceService).addSpanAttributes(argThat(map -> 
            map.containsKey("order.id") && map.get("order.id").equals(orderId)));
        verify(traceService).addSpanEvent(eq("Order found"), anyMap());
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnNotFound() {
        String orderId = "nonexistent";
        when(orderDbService.findById(orderId)).thenReturn(Optional.empty());
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());
        doNothing().when(metricsService).incrementOrderFailed();

        ResponseEntity<?> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof CreateOrderResponse);
        CreateOrderResponse orderResponse = (CreateOrderResponse) response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals("NOT_FOUND", orderResponse.getStatus());
        assertTrue(orderResponse.getMessage().contains("not found"));

        verify(traceService).addSpanEvent(eq("Order not found"), anyMap());
        verify(metricsService).incrementOrderFailed();
    }

    @Test
    void payOrder_WhenOrderExistsAndStatusIsCreated_ShouldUpdateStatusToPaid() {
        // Arrange
        String orderId = "order123";
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("CREATED");
        mockOrder.setCustomerId("customer123");

        when(orderDbService.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(orderDbService).save(any(Order.class));
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());
        doNothing().when(metricsService).incrementOrderPaid();

        // Act
        ResponseEntity<?> response = orderController.payOrder(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof CreateOrderResponse);
        CreateOrderResponse orderResponse = (CreateOrderResponse) response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals("PAID", orderResponse.getStatus());
        assertEquals("Order successfully paid", orderResponse.getMessage());

        // Verify order was updated
        assertEquals("PAID", mockOrder.getStatus());
        verify(orderDbService).save(mockOrder);
        
        // Verify metrics
        verify(metricsService).incrementOrderPaid();
    }

    @Test
    void payOrder_WhenOrderExistsButWrongStatus_ShouldReturnBadRequest() {
        // Arrange
        String orderId = "order123";
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("PAID"); // Already paid
        mockOrder.setCustomerId("customer123");

        when(orderDbService.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());

        // Act
        ResponseEntity<?> response = orderController.payOrder(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof CreateOrderResponse);
        CreateOrderResponse orderResponse = (CreateOrderResponse) response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals("PAID", orderResponse.getStatus());
        assertTrue(orderResponse.getMessage().contains("cannot be paid"));

        // Verify order was not saved again
        verify(orderDbService, never()).save(any(Order.class));
        
        // Verify metrics were not incremented
        verify(metricsService, never()).incrementOrderPaid();
    }

    @Test
    void payOrder_WhenOrderDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        String orderId = "nonexistent";
        when(orderDbService.findById(orderId)).thenReturn(Optional.empty());
        doNothing().when(traceService).addSpanAttributes(anyMap());
        doNothing().when(traceService).addSpanEvent(anyString(), anyMap());
        doNothing().when(metricsService).incrementOrderFailed();

        // Act
        ResponseEntity<?> response = orderController.payOrder(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof CreateOrderResponse);
        CreateOrderResponse orderResponse = (CreateOrderResponse) response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals("NOT_FOUND", orderResponse.getStatus());
        assertTrue(orderResponse.getMessage().contains("not found"));

        // Verify metrics
        verify(metricsService).incrementOrderFailed();
    }

}