package com.bloomstudio.api.repository;

import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByStripeSessionId(String stripeSessionId);
    List<Order> findByStatus(OrderStatus status);
}
