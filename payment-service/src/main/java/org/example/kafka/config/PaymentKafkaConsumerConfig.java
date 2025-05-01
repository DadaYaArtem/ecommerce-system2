package org.example.kafka.config;

import org.example.events.DeadLetterRecord;
import org.example.events.PaymentRequestEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.io.PrintWriter;
import java.io.StringWriter;

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
                    // Получаем оригинальное сообщение
                    PaymentRequestEvent event = (PaymentRequestEvent) record.value();

                    // Генерируем stack trace в текстовый вид
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    String stackTrace = sw.toString();

                    // Создаём DeadLetterRecord
                    DeadLetterRecord<PaymentRequestEvent> dlqMessage = new DeadLetterRecord<>(
                            event,
                            ex.getClass().getName(),
                            ex.getMessage(),
                            stackTrace
                    );

                    // Отправляем в DLQ
                    kafkaTemplate.send(record.topic() + ".DLT", dlqMessage);

                    // Возвращаем null, чтобы Spring больше ничего не пересылал
                    return null;
                }
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setAckAfterHandle(true);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        errorHandler.setAckAfterHandle(true);

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
