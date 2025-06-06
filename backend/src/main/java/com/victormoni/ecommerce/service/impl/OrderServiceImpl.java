package com.victormoni.ecommerce.service.impl;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.dto.request.OrderItemRequest;
import com.victormoni.ecommerce.dto.request.OrderRequest;
import com.victormoni.ecommerce.dto.response.OrderResponse;
import com.victormoni.ecommerce.exception.BusinessException;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.mapper.OrderMapper;
import com.victormoni.ecommerce.model.Order;
import com.victormoni.ecommerce.model.OrderItem;
import com.victormoni.ecommerce.model.OrderStatus;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.repository.OrderRepository;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.service.OrderService;
import com.victormoni.ecommerce.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Victor Moni
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID " + id));
        return OrderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByUser(String username, Pageable pageable) {
        return orderRepository.findByUser_Username(username, pageable)
                .map(OrderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(OrderMapper::toDTO);
    }

    @Override
    @Transactional
    public OrderResponse create(String username, OrderRequest dto) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Order order = new Order();
        order.setUser(user);

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> createOrderItem(order, itemDto))
                .collect(Collectors.toList());

        order.getItems().addAll(items);
        order.calculateTotal();

        Order saved = orderRepository.save(order);
        return OrderMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public OrderResponse update(String username, Long id, OrderRequest request) {

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para atualizar este pedido.");
        }

        order.getItems().clear();

        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> createOrderItem(order, itemDto))
                .collect(Collectors.toList());

        order.getItems().addAll(items);
        order.calculateTotal();

        Order updated = orderRepository.save(order);
        return OrderMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido não encontrado com ID " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest itemDto) {
        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID " + itemDto.getProductId()));

        if (product.getStock() < itemDto.getQuantity()) {
            throw new BusinessException("Estoque insuficiente para o produto " + product.getName());
        }

        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .quantity(itemDto.getQuantity())
                .unitPrice(product.getPrice())
                .build();
    }
}
