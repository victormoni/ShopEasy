package com.shopeasy.ecommerce.service;

import com.shopeasy.ecommerce.dto.request.OrderRequest;
import com.shopeasy.ecommerce.dto.response.OrderResponse;
import com.shopeasy.ecommerce.model.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    List<OrderResponse> list();

    OrderResponse findById(Long id);

    Page<OrderResponse> findByUser(String username, Pageable pageable);

    Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable);

    OrderResponse create(String username, OrderRequest dto);

    OrderResponse update(String username, Long id, OrderRequest request);

    void delete(Long id);
}
