package com.shopeasy.ecommerce.repository;

import com.shopeasy.ecommerce.model.Order;
import com.shopeasy.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser_Username(String username, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
