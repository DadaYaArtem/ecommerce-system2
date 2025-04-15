package org.example.listener;

import org.example.events.PriceRequestEvent;
import org.example.events.PriceResponseEvent;
import org.example.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PRICE_REQUESTS;
import static org.example.kafka.constants.KafkaTopics.PRICE_RESPONSES;
import static org.example.kafka.constants.KafkaGroups.PRODUCT_SERVICE;

@Component
public class PriceRequestListener {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PriceRequestListener(ProductService productService,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = PRICE_REQUESTS, groupId = PRODUCT_SERVICE)
    public void handlePriceRequest(PriceRequestEvent event) {
        System.out.println("🔍 Product-service отримав PriceRequestEvent: " + event);

        double price = productService.getPrice(event.getProductId());

        PriceResponseEvent response = new PriceResponseEvent(
                event.getOrderId(),
                event.getProductId(),
                price
        );

        kafkaTemplate.send(PRICE_RESPONSES, response);
        System.out.println("📤 Відправлено PriceResponseEvent: " + response);
    }
}
