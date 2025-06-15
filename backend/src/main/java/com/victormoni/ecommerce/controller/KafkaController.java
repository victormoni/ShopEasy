/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.victormoni.ecommerce.controller;

import com.victormoni.ecommerce.api.KafkaApi;
import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import com.victormoni.ecommerce.kafka.producer.KafkaProducerService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Victor Moni
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kafka")
public class KafkaController implements KafkaApi{

    private final KafkaProducerService kafkaProducerService;

    @PostMapping
    public ResponseEntity<String> sendEvent(@RequestParam String status) {
        OrderEvent event = new OrderEvent(1L, 100L, new BigDecimal("199.99"), status);
        kafkaProducerService.sendOrderEvent(event);
        return ResponseEntity.ok("Evento enviado com status: " + status);
    }
}