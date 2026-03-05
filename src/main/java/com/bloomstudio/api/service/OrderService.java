package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.OrderRequest;
import com.bloomstudio.api.dto.response.OrderResponse;
import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.entity.OrderItem;
import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.OrderStatus;
import com.bloomstudio.api.exception.BadRequestException;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.OrderRepository;
import com.bloomstudio.api.repository.ProductRepository;
import com.bloomstudio.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<OrderResponse> getMyOrders(String email) {
        User user = findUserByEmail(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getMyOrder(Long orderId, String email) {
        Order order = findById(orderId);
        User user = findUserByEmail(email);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Accès refusé");
        }
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getById(Long id) {
        return OrderResponse.from(findById(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request, String email) {
        User user = findUserByEmail(email);

        Order order = Order.builder()
                .orderNumber("BS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryZip(request.getDeliveryZip())
                .deliveryCity(request.getDeliveryCity())
                .deliveryMethod(request.getDeliveryMethod())
                .deliveryNotes(request.getDeliveryNotes())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé : " + itemReq.getProductId()));

            if (!product.getInStock()) {
                throw new BadRequestException("Produit en rupture de stock : " + product.getName());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(lineTotal)
                    .build();
            order.addItem(item);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal shippingCost = subtotal.compareTo(BigDecimal.valueOf(80)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(6.90);

        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setTotal(subtotal.add(shippingCost));

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long orderId, String email) {
        Order order = findById(orderId);
        User user = findUserByEmail(email);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Accès refusé");
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Impossible d'annuler une commande déjà expédiée");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(orderRepository.save(order));
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée : " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}
