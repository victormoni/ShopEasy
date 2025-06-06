package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Victor Moni
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldCreateProductSuccessfully() {
        ProductRequest request = ProductRequest.builder()
                .name("Camiseta Azul")
                .description("Algodão")
                .price(BigDecimal.TEN)
                .stock(10)
                .build();

        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = productService.create(request);

        assertEquals("Camiseta Azul", response.getName());
        assertEquals(BigDecimal.TEN, response.getPrice());
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Product existing = Product.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Desc")
                .price(BigDecimal.ONE)
                .stock(1)
                .build();

        ProductRequest dto = ProductRequest.builder()
                .name("New Name")
                .description("New Desc")
                .price(BigDecimal.TEN)
                .stock(20)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = productService.update(1L, dto);

        assertEquals("New Name", result.getName());
        assertEquals(BigDecimal.TEN, result.getPrice());
        assertEquals(20, result.getStock());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
    }
}
