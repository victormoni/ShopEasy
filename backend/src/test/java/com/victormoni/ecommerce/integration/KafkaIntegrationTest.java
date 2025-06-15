/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.victormoni.ecommerce.integration;

import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

/**
 *
 * @author Victor Moni
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "order-events", brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Test
    void testSendMessage() throws Exception {
        OrderEvent event = new OrderEvent(1L, 2L, new BigDecimal("100.00"), "PENDING");
        kafkaTemplate.send("order-events", event);
        // Use algum listener de teste ou timeout + verificação via assert.
    }
}