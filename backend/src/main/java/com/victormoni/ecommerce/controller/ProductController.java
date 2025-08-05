package com.victormoni.ecommerce.controller;

import com.victormoni.ecommerce.api.ProductApi;
import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.dto.response.ProductResponse;
import com.victormoni.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Produtos", description = "Operações de gerenciamento de produtos")
public class ProductController implements ProductApi {

    private final ProductService service;

    @Override
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📦 Listando produtos com filtros - name: {}, category: {}, minPrice: {}, maxPrice: {}", name, category, minPrice, maxPrice);
        Page<ProductResponse> resultPage = service.list(name, category, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(resultPage);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        log.info("🔍 Buscando produto com ID: {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest dto) {
        log.info("🆕 Criando novo produto: {}", dto.getName());
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest dto) {
        log.info("✏️ Atualizando produto com ID: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("❌ Excluindo produto com ID: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}