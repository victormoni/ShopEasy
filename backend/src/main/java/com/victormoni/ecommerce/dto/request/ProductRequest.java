package com.victormoni.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @Schema(example = "Camiseta Azul", description = "Nome do produto")
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100)
    private String name;

    @Schema(example = "Camiseta 100% algodão", description = "Descrição detalhada do produto")
    @Size(max = 1000)
    private String description;

    @Schema(example = "99.90", description = "Preço do produto")
    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private BigDecimal price;

    @Schema(example = "10", description = "Quantidade em estoque")
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock;
}
