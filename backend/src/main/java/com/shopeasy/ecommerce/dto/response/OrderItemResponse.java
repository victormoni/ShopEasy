package com.shopeasy.ecommerce.dto.response;

import com.shopeasy.ecommerce.model.OrderItem;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;

    public static OrderItemResponse fromEntity(OrderItem item) {
        BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .total(total)
                .build();
    }
}
