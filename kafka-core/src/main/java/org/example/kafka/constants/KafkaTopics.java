package org.example.kafka.constants;

/**
 * Центральное место для всех Kafka-топиков.
 * Используйте эти константы в продюсерах и листенерах.
 */
public class KafkaTopics {

    public static final String ORDER_EVENTS = "order-events";
    public static final String INVENTORY_EVENTS = "inventory-events";
    public static final String PAYMENT_REQUESTS = "payment-requests";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String PRICE_REQUESTS = "price-requests";
    public static final String PRICE_RESPONSES = "price-responses";

    public static final String INVENTORY_RELEASES      = "inventory-release-events";  // <-- новый

    private KafkaTopics() {
        // утилитарный класс — не создавать экземпляры
    }
}
