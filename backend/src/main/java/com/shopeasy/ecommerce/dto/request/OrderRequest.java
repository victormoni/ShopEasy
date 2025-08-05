package com.shopeasy.ecommerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Deve haver ao menos 1 item no pedido")
    private List<@NotNull OrderItemRequest> items;
}
