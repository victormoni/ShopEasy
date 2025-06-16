package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

import com.victormoni.ecommerce.dto.request.OrderRequest;
import com.victormoni.ecommerce.dto.response.OrderResponse;
import com.victormoni.ecommerce.model.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*
 * @author Victor Moni
 */

public interface OrderService {

    List<OrderResponse> list();

    OrderResponse findById(Long id);

    Page<OrderResponse> findByUser(String username, Pageable pageable);

    Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable);

    OrderResponse create(String username, OrderRequest dto);

    OrderResponse update(String username, Long id, OrderRequest request);

    void delete(Long id);
}
