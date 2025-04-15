package org.example.messaging;

import org.example.events.PriceRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import static org.example.kafka.constants.KafkaTopics.PRICE_REQUESTS;


@Component
public class PriceRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PriceRequestProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPriceRequest(PriceRequestEvent event) {
        System.out.println("📤 Надіслано PriceRequestEvent: " + event);
        kafkaTemplate.send(PRICE_REQUESTS, event);
    }
}
