package org.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.events.PaymentRequestEvent;
import org.example.events.PaymentRequestFailedEvent;  // Импортируем класс с ошибкой
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentKafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> paymentKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Повторяем 3 раза с паузой 1 с, затем в DLQ
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // Создаём событие с ошибкой и отправляем в DLQ
                    PaymentRequestFailedEvent failedEvent = new PaymentRequestFailedEvent(
                            ((PaymentRequestEvent) record.value()).getOrderId(),
                            ((PaymentRequestEvent) record.value()).getTotalAmount(),
                            ex.getMessage()  // Добавляем сообщение об ошибке
                    );
                    kafkaTemplate.send(record.topic() + ".DLT", failedEvent);
                    return null;
                }
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
