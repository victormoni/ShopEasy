package com.victormoni.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;

    @Schema(example = "2025-05-28 15:42:12", description = "Data de criação do pedido")
    private String createdAt;
    private BigDecimal total;
    private List<OrderItemResponse> items;
}
