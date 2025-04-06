package org.example.listener;

import org.example.event.PriceRequestEvent;
import org.example.event.PriceResponseEvent;
import org.example.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PriceRequestListener {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PriceRequestListener(ProductService productService,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "price-requests", groupId = "product-group")
    public void handlePriceRequest(PriceRequestEvent event) {
        System.out.println("🔍 Product-service отримав запит на ціну: " + event);

        double price = productService.getPrice(event.getProductId());

        PriceResponseEvent response = new PriceResponseEvent(
                event.getOrderId(),
                event.getProductId(),
                price
        );

        kafkaTemplate.send("price-responses", response);
        System.out.println("📤 Відповідь з ціною: " + response);
    }
}
