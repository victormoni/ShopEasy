package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.victormoni.ecommerce.dto.request.OrderItemRequest;
import com.victormoni.ecommerce.dto.request.OrderRequest;
import com.victormoni.ecommerce.exception.BusinessException;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import com.victormoni.ecommerce.kafka.producer.KafkaProducerService;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.repository.OrderRepository;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * @author Victor Moni
 */

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserService userService;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldCreateOrderSuccessfully() {
        String username = "fulano";
        User user = User.builder().id(1L).username(username).build();
        Product product = Product.builder().id(1L).name("Produto A").price(BigDecimal.TEN).stock(5).build();

        OrderItemRequest item = OrderItemRequest.builder().productId(1L).quantity(2).build();
        OrderRequest request = OrderRequest.builder().items(List.of(item)).build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = orderService.create(username, request);

        assertEquals(1, response.getItems().size());
        assertTrue(response.getTotal().compareTo(new BigDecimal("20.00")) == 0);
        verify(kafkaProducerService, times(1)).sendOrderEvent(any(OrderEvent.class));
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        String username = "fulano";
        User user = User.builder().id(1L).username(username).build();

        OrderItemRequest item = OrderItemRequest.builder().productId(1L).quantity(1).build();
        OrderRequest request = OrderRequest.builder().items(List.of(item)).build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(username, request));
    }

    @Test
    void shouldThrowWhenInsufficientStock() {
        String username = "fulano";
        User user = User.builder().id(1L).username(username).build();
        Product product = Product.builder().id(1L).name("Produto A").stock(1).build();

        OrderItemRequest item = OrderItemRequest.builder().productId(1L).quantity(2).build();
        OrderRequest request = OrderRequest.builder().items(List.of(item)).build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () -> orderService.create(username, request));
    }
}
