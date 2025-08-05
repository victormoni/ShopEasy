package com.shopeasy.ecommerce.service.impl;

import com.shopeasy.ecommerce.dto.request.OrderItemRequest;
import com.shopeasy.ecommerce.dto.request.OrderRequest;
import com.shopeasy.ecommerce.dto.response.OrderResponse;
import com.shopeasy.ecommerce.exception.BusinessException;
import com.shopeasy.ecommerce.exception.ResourceNotFoundException;
import com.shopeasy.ecommerce.kafka.dto.OrderEvent;
import com.shopeasy.ecommerce.kafka.producer.KafkaProducerService;
import com.shopeasy.ecommerce.mapper.OrderMapper;
import com.shopeasy.ecommerce.model.Order;
import com.shopeasy.ecommerce.model.OrderItem;
import com.shopeasy.ecommerce.model.OrderStatus;
import com.shopeasy.ecommerce.model.Product;
import com.shopeasy.ecommerce.model.User;
import com.shopeasy.ecommerce.repository.OrderRepository;
import com.shopeasy.ecommerce.repository.ProductRepository;
import com.shopeasy.ecommerce.service.OrderService;
import com.shopeasy.ecommerce.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        log.info("📋 Listando todos os pedidos");
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        log.info("🔍 Buscando pedido por ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Pedido não encontrado: ID {}", id);
                    return new ResourceNotFoundException("Pedido não encontrado com ID " + id);
                });
        return OrderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByUser(String username, Pageable pageable) {
        log.info("📄 Buscando pedidos do usuário: {}", username);
        return orderRepository.findByUser_Username(username, pageable)
                .map(OrderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
        log.info("📂 Buscando pedidos com status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(OrderMapper::toDTO);
    }

    @Override
    @Transactional
    public OrderResponse create(String username, OrderRequest dto) {
        log.info("🛒 Criando novo pedido para usuário: {}", username);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("❌ Usuário não encontrado: {}", username);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });

        Order order = new Order();
        order.setUser(user);

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> createOrderItem(order, itemDto))
                .collect(Collectors.toList());

        order.getItems().addAll(items);
        order.calculateTotal();
        order.setStatus(OrderStatus.NEW);   
        Order saved = orderRepository.save(order);
        log.info("✅ Pedido criado com ID: {}", saved.getId());

        OrderEvent event = new OrderEvent(saved.getId(), user.getId(), saved.getTotal(), saved.getStatus().name());
        kafkaProducerService.sendOrderEvent(event);
        log.info("📤 Evento enviado para Kafka: {}", event);

        return OrderMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public OrderResponse update(String username, Long id, OrderRequest request) {
        log.info("✏️ Atualizando pedido ID {} do usuário {}", id, username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("🚫 Usuário {} tentou alterar pedido que não pertence a ele", username);
            throw new BusinessException("Você não tem permissão para atualizar este pedido.");
        }

        order.getItems().clear();
        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> createOrderItem(order, itemDto))
                .collect(Collectors.toList());

        order.getItems().addAll(items);
        order.calculateTotal();

        Order updated = orderRepository.save(order);
        log.info("✅ Pedido atualizado: ID {}", updated.getId());

        return OrderMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("🗑️ Deletando pedido com ID {}", id);
        if (!orderRepository.existsById(id)) {
            log.warn("❌ Pedido não encontrado para deletar: {}", id);
            throw new ResourceNotFoundException("Pedido não encontrado com ID " + id);
        }
        orderRepository.deleteById(id);
        log.info("✅ Pedido removido com sucesso: ID {}", id);
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest itemDto) {
        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> {
                    log.warn("❌ Produto não encontrado: ID {}", itemDto.getProductId());
                    return new ResourceNotFoundException("Produto não encontrado com ID " + itemDto.getProductId());
                });

        if (product.getStock() < itemDto.getQuantity()) {
            log.warn("🚫 Estoque insuficiente para o produto {} (solicitado: {}, disponível: {})",
                    product.getName(), itemDto.getQuantity(), product.getStock());
            throw new BusinessException("Estoque insuficiente para o produto " + product.getName());
        }

        log.info("✅ Item adicionado ao pedido: produto={}, quantidade={}", product.getName(), itemDto.getQuantity());

        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .quantity(itemDto.getQuantity())
                .unitPrice(product.getPrice())
                .build();
    }
}