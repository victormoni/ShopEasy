package com.victormoni.ecommerce.kafka.producer;

import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "order-events";

    public void sendOrderEvent(OrderEvent event) {
        log.info("🚀 Enviando evento ao Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}