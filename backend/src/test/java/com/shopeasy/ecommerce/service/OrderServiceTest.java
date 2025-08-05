package com.shopeasy.ecommerce.service;

import com.shopeasy.ecommerce.dto.request.OrderItemRequest;
import com.shopeasy.ecommerce.dto.request.OrderRequest;
import com.shopeasy.ecommerce.exception.BusinessException;
import com.shopeasy.ecommerce.exception.ResourceNotFoundException;
import com.shopeasy.ecommerce.kafka.dto.OrderEvent;
import com.shopeasy.ecommerce.kafka.producer.KafkaProducerService;
import com.shopeasy.ecommerce.model.Product;
import com.shopeasy.ecommerce.model.User;
import com.shopeasy.ecommerce.repository.OrderRepository;
import com.shopeasy.ecommerce.repository.ProductRepository;
import com.shopeasy.ecommerce.service.impl.OrderServiceImpl;
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
