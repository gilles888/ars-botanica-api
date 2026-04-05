package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.OrderRequest;
import com.bloomstudio.api.dto.response.OrderResponse;
import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.entity.OrderItem;
import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.entity.ProductVariant;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.OrderStatus;
import com.bloomstudio.api.exception.BadRequestException;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.OrderRepository;
import com.bloomstudio.api.repository.ProductRepository;
import com.bloomstudio.api.repository.ProductVariantRepository;
import com.bloomstudio.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
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
        return OrderResponse.from(createOrderEntity(request, email));
    }

    @Transactional
    public Order createOrderEntity(OrderRequest request, String email) {
        User user = findUserByEmail(email);

        Order order = Order.builder()
                .orderNumber("BS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .status(OrderStatus.PAYMENT_PENDING)
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

            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante non trouvée : " + itemReq.getVariantId()));

            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException(
                        "La variante " + itemReq.getVariantId() + " n'appartient pas au produit " + itemReq.getProductId());
            }

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .variant(variant)
                    .size(variant.getSize())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
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

        return orderRepository.save(order);
    }

    @Transactional
    public void updateStripeSession(Long orderId, String stripeSessionId) {
        Order order = findById(orderId);
        order.setStripeSessionId(stripeSessionId);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmPayment(String stripeSessionId) {
        orderRepository.findByStripeSessionId(stripeSessionId).ifPresent(order -> {
            if (order.getStatus() != OrderStatus.PAID) {
                order.setStatus(OrderStatus.PAID);
                // updatedAt est mis à jour automatiquement via @PreUpdate
                orderRepository.save(order);
            }
        });
    }

    /**
     * Indique si une commande associée à une session Stripe est déjà au statut PAID.
     * Utilisé par le controller pour éviter une double confirmation.
     *
     * @param stripeSessionId l'identifiant de la session Stripe
     * @return true si la commande est déjà PAID, false sinon ou si introuvable
     */
    public boolean isAlreadyPaid(String stripeSessionId) {
        return orderRepository.findByStripeSessionId(stripeSessionId)
                .map(order -> order.getStatus() == OrderStatus.PAID)
                .orElse(false);
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

    @Transactional
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
            log.info("Commande {} annulée suite à échec Stripe", order.getOrderNumber());
        });
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
