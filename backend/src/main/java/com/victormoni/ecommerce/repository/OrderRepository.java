package com.victormoni.ecommerce.repository;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
import com.victormoni.ecommerce.model.Order;
import com.victormoni.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Victor Moni
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser_Username(String username, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
