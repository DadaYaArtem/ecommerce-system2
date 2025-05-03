package org.example.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class MetricsService {

    private final MeterRegistry registry;
    private final Counter orderCreatedCounter;
    private final Counter orderPaidCounter;
    private final Counter orderFailedCounter;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.orderCreatedCounter = Counter.builder("order.created")
                .description("Total number of orders created")
                .register(registry);
        this.orderPaidCounter = Counter.builder("order.paid")
                .description("Total number of successfully paid orders")
                .register(registry);
        this.orderFailedCounter = Counter.builder("order.failed")
                .description("Total number of failed orders")
                .register(registry);
    }

    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
    }

    public void incrementOrderPaid() {
        orderPaidCounter.increment();
    }

    public void incrementOrderFailed() {
        orderFailedCounter.increment();
    }

    public <T> T recordTimedOperation(String name, Supplier<T> operation) {
        Timer timer = Timer.builder("operation.time")
                .tag("operation", name)
                .description("Time taken for operations")
                .register(registry);

        long start = System.nanoTime();
        try {
            return operation.get();
        } finally {
            long end = System.nanoTime();
            timer.record(end - start, TimeUnit.NANOSECONDS);
        }
    }

    public void recordTimedOperation(String name, Runnable operation) {
        Timer timer = Timer.builder("operation.time")
                .tag("operation", name)
                .description("Time taken for operations")
                .register(registry);

        timer.record(() -> operation.run());
    }
}