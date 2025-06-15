/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.victormoni.ecommerce.kafka.consumer;

import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author Victor Moni
 */
@Slf4j
@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "order-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        log.info("📩 Evento recebido no Kafka: {}", event);
    }
}