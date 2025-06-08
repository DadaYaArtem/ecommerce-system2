package org.example.controller;

import org.example.dto.DeliveryStatusResponse;
import org.example.model.Delivery;
import org.example.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryStatusResponse> getDeliveryStatus(@PathVariable String orderId) {
        Delivery delivery = deliveryService.findByOrderId(orderId);
        if (delivery == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DeliveryStatusResponse(orderId, null, null, "Delivery not found"));
        }
        DeliveryStatusResponse response = new DeliveryStatusResponse(
                delivery.getOrderId(),
                delivery.getStatus(),
                delivery.getId(),
                null
        );
        return ResponseEntity.ok(response);
    }
}
