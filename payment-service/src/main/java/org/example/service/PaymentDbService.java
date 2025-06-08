package org.example.service;

import org.example.gateway.PaymentStatus;
import org.example.model.Payment;
import org.example.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentDbService {

    private final PaymentRepository repository;

    public PaymentDbService(PaymentRepository repository) {
        this.repository = repository;
    }

    public Payment create(Payment payment) {
        return repository.save(payment);
    }

    public Payment findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Transactional
    public void updateStatus(String paymentId, PaymentStatus status,
                             String transactionId, String errorMessage) {
        repository.findById(paymentId).ifPresent(p -> {
            p.setStatus(status);
            p.setTransactionId(transactionId);
            p.setErrorMessage(errorMessage);
            p.setUpdatedAt(LocalDateTime.now());
            repository.save(p);
        });
    }
}
