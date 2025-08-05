package com.shopeasy.ecommerce.kafka.consumer;

import com.shopeasy.ecommerce.kafka.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "order-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        log.info("📩 Evento recebido no Kafka: {}", event);
    }
}