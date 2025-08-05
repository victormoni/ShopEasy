package com.victormoni.ecommerce.spec;

import com.victormoni.ecommerce.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class ProductSpecifications {

    public static Specification<Product> filterBy(
            String nameContains,
            String categoryEquals,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // 1) Filtro por nome (LIKE %…%)
            if (nameContains != null && !nameContains.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + nameContains.toLowerCase() + "%"
                        ));
            }

            // 2) Filtro por categoria exata
            if (categoryEquals != null && !categoryEquals.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("category"), categoryEquals));
            }

            // 3) Filtro por preço mínimo
            if (minPrice != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // 4) Filtro por preço máximo
            if (maxPrice != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicate;
        };
    }
}
