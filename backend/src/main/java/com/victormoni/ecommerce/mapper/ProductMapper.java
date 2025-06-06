package com.victormoni.ecommerce.mapper;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.dto.response.ProductResponse;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.util.FormatUtil;
import java.util.Objects;

/**
 *
 * @author Victor Moni
 */
public class ProductMapper {

    public static Product toEntity(ProductRequest dto) {
        Objects.requireNonNull(dto, "ProductRequest não pode ser nulo");
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build();
    }

    public static ProductResponse toResponseDTO(Product p) {
        if (p == null) {
            return null;
        }
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .category(p.getCategory())
                .createdAt(p.getCreatedAt() == null
                        ? null
                        : p.getCreatedAt().format(FormatUtil.DATE_TIME_FORMATTER))
                .build();
    }

    public static void updateEntity(Product p, ProductRequest dto) {
        if (p == null || dto == null) {
            return;
        }
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
    }
}
