package org.example.controller;

import org.example.dto.CreateOrderRequest;
import org.example.dto.CreateOrderResponse;
import org.example.events.OrderCreatedEvent;
import org.example.messaging.OrderEventProducer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.service.OrderDbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderDbService orderDbService;
    private final OrderEventProducer orderProducer;

    public OrderController(OrderDbService orderDbService,
                           OrderEventProducer orderProducer) {
        this.orderDbService = orderDbService;
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");

        for (CreateOrderRequest.ItemRequest item : request.getItems()) {
            OrderItem orderItem = new OrderItem(item.getProductId(), item.getQuantity(), order);
            order.getItems().add(orderItem);
        }

        orderDbService.save(order);

        // пока отправим только первый товар — для совместимости со старой логикой
//        OrderItem firstItem = order.getItems().get(0);

        for (OrderItem item : order.getItems()) {
            orderProducer.sendOrderCreatedEvent(new OrderCreatedEvent(
                    order.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    order.getCustomerId()
            ));
        }

        CreateOrderResponse response = new CreateOrderResponse(
                order.getId(),
                "CREATED",
                "Order created successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        return orderDbService.findById(id)
                .map(order -> ResponseEntity.ok(new CreateOrderResponse(
                        order.getId(),
                        order.getStatus(),
                        "Order status fetched successfully"
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CreateOrderResponse(id, "NOT_FOUND", "❌ Order not found")));
    }

}
