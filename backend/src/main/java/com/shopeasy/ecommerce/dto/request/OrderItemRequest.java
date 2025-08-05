package com.shopeasy.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @Schema(example = "1", description = "ID do produto")
    @NotNull
    private Long productId;

    @Schema(example = "2", description = "Quantidade do produto")
    @NotNull
    @Positive
    private Integer quantity;
}
