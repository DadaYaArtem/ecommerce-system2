package org.example.controller;

import org.example.dto.PaymentStatusResponse;
import org.example.model.Payment;
import org.example.service.PaymentDbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentDbService paymentDbService;

    public PaymentController(PaymentDbService paymentDbService) {
        this.paymentDbService = paymentDbService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String orderId) {
        Payment payment = paymentDbService.findByOrderId(orderId);
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PaymentStatusResponse(orderId, null, null, "Payment not found"));
        }
        PaymentStatusResponse response = new PaymentStatusResponse(
                payment.getOrderId(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getErrorMessage()
        );
        return ResponseEntity.ok(response);
    }
}
