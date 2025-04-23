package org.example.gateway;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        boolean isSuccessful = Math.random() > 0.2;

        if (isSuccessful) {
            return new PaymentResult(
                UUID.randomUUID().toString(),
                PaymentStatus.SUCCESSFUL,
                LocalDateTime.now()
            );
        } else {
            return new PaymentResult(
                UUID.randomUUID().toString(),
                PaymentStatus.DECLINED,
                LocalDateTime.now(),
                "Insufficient funds"
            );
        }
    }
}