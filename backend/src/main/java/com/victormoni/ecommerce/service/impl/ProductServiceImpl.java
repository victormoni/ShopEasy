package com.victormoni.ecommerce.service.impl;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Victor Moni
 */
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

        // 1) Monta Specification a partir dos parâmetros
        Specification<Product> spec = ProductSpecifications.filterBy(name, category, minPrice, maxPrice);

        // 2) Cria um Pageable ordenando por “name”
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        // 3) Executa findAll(spec, pageable), que retorna Page<Product>
        Page<Product> pageResult = productRepository.findAll(spec, pageable);

        // 4) Converte cada entidade Product em ProductResponse e empacota em Page<ProductResponse>
        List<ProductResponse> responseList = pageResult.getContent()
                .stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(
                responseList,
                pageable,
                pageResult.getTotalElements()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
        return ProductMapper.toResponseDTO(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequestDTO) {
        Product product = ProductMapper.toEntity(productRequestDTO);
        Product saved = productRepository.save(product);
        return ProductMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest productRequestDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
        existing.updateFrom(productRequestDTO);
        Product updated = productRepository.save(existing);

        return ProductMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado: " + id);
        }
        productRepository.deleteById(id);
    }
}
