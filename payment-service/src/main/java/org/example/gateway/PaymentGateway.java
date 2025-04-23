package org.example.gateway;

public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}