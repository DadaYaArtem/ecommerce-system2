package org.example.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.DeadLetterRecord;
import org.example.events.PaymentRequestEvent;
import org.example.events.PaymentFailedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentDlqListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Для ручного парсинга

    public PaymentDlqListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "payment-requests.DLT", groupId = "payment-dlq-processor", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, DeadLetterRecord<Object>> record) {
        DeadLetterRecord<Object> raw = record.value();

        // Пробуем десериализовать payload вручную
        PaymentRequestEvent event = objectMapper.convertValue(raw.getPayload(), PaymentRequestEvent.class);

        System.out.println("❗ Обнаружено сообщение в DLQ для заказа: " + event.getOrderId());

        PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                event.getOrderId(),
                event.getTotalAmount()
        );

        kafkaTemplate.send("payment-events", failedEvent);
    }
}
