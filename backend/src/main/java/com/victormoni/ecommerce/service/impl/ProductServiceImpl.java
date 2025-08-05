package com.victormoni.ecommerce.service.impl;

import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.dto.response.ProductResponse;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.mapper.ProductMapper;
import com.victormoni.ecommerce.model.Product;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.service.ProductService;
import com.victormoni.ecommerce.spec.ProductSpecifications;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String name,
                                      String category,
                                      BigDecimal minPrice,
                                      BigDecimal maxPrice,
                                      int page,
                                      int size) {

        log.info("📦 Buscando produtos com filtros - nome: {}, categoria: {}, min: {}, max: {}, página: {}, tamanho: {}",
                name, category, minPrice, maxPrice, page, size);

        Specification<Product> spec = ProductSpecifications.filterBy(name, category, minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        Page<Product> pageResult = productRepository.findAll(spec, pageable);

        List<ProductResponse> responseList = pageResult.getContent()
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());

        log.info("✅ {} produtos encontrados", responseList.size());

        return new PageImpl<>(responseList, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        log.info("🔍 Buscando produto por ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Produto não encontrado: ID {}", id);
                    return new ResourceNotFoundException("Produto não encontrado: " + id);
                });
        return ProductMapper.toResponseDTO(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequestDTO) {
        log.info("🛠️ Criando novo produto: {}", productRequestDTO.getName());
        Product product = ProductMapper.toEntity(productRequestDTO);
        Product saved = productRepository.save(product);
        log.info("✅ Produto salvo com ID: {}", saved.getId());
        return ProductMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest productRequestDTO) {
        log.info("✏️ Atualizando produto ID {}", id);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Produto não encontrado para atualização: {}", id);
                    return new ResourceNotFoundException("Produto não encontrado: " + id);
                });

        existing.updateFrom(productRequestDTO);
        Product updated = productRepository.save(existing);
        log.info("✅ Produto atualizado com sucesso: ID {}", updated.getId());

        return ProductMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("🗑️ Deletando produto com ID: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("❌ Produto não encontrado para exclusão: {}", id);
            throw new ResourceNotFoundException("Produto não encontrado: " + id);
        }
        productRepository.deleteById(id);
        log.info("✅ Produto removido com sucesso: ID {}", id);
    }
}