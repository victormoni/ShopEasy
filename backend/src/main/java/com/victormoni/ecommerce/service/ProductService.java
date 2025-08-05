package com.victormoni.ecommerce.service;

import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.dto.response.ProductResponse;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;

public interface ProductService {

    Page<ProductResponse> list(String name, String category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest dto);

    ProductResponse update(Long id, ProductRequest dto);

    void delete(Long id);
}
