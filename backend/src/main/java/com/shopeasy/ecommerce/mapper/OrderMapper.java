package com.shopeasy.ecommerce.mapper;

import com.shopeasy.ecommerce.dto.request.OrderRequest;
import com.shopeasy.ecommerce.dto.response.OrderItemResponse;
import com.shopeasy.ecommerce.dto.response.OrderResponse;
import com.shopeasy.ecommerce.exception.ResourceNotFoundException;
import com.shopeasy.ecommerce.model.Order;
import com.shopeasy.ecommerce.model.OrderItem;
import com.shopeasy.ecommerce.model.OrderStatus;
import com.shopeasy.ecommerce.model.Product;
import com.shopeasy.ecommerce.repository.ProductRepository;
import com.shopeasy.ecommerce.util.FormatUtil;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
