package com.victormoni.ecommerce.mapper;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.dto.request.OrderRequest;
import com.victormoni.ecommerce.dto.response.OrderItemResponse;
import com.victormoni.ecommerce.dto.response.OrderResponse;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.model.Order;
import com.victormoni.ecommerce.model.OrderItem;
import com.victormoni.ecommerce.model.OrderStatus;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.util.FormatUtil;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author Victor Moni
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderMapper {

    public static Order toEntity(OrderRequest dto, ProductRepository repo) {

        Order order = Order.builder()
                .status(OrderStatus.NEW)
                .items(new ArrayList<>())
                .build();

        dto.getItems().forEach(i -> {
            Product p = repo.findById(i.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

            order.getItems().add(
                    OrderItem.builder()
                            .order(order)
                            .product(p)
                            .quantity(i.getQuantity())
                            .productName(p.getName())
                            .unitPrice(p.getPrice())
                            .build()
            );
        });
        return order;
    }

    public static OrderResponse toDTO(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .createdAt(order.getCreatedAt() == null ? null : order.getCreatedAt().format(FormatUtil.DATE_TIME_FORMATTER))
                .total(order.getTotal())
                .items(order.getItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.<OrderItemResponse>toList())
                )
                .build();
    }
}
