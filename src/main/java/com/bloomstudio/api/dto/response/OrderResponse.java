package com.bloomstudio.api.dto.response;

import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.entity.OrderItem;
import com.bloomstudio.api.enums.OrderStatus;
import com.bloomstudio.api.enums.ProductSize;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private List<OrderItemResponse> items;
    private OrderStatus status;
    private String deliveryAddress;
    private String deliveryZip;
    private String deliveryCity;
    private String deliveryMethod;
    private String deliveryNotes;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private LocalDateTime createdAt;

    @Data @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private Long variantId;
        private ProductSize size;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productSlug(item.getProduct().getSlug())
                    .productImage(item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()
                            ? item.getProduct().getImages().get(0) : null)
                    .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                    .size(item.getSize())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .userId(o.getUser().getId())
                .userEmail(o.getUser().getEmail())
                .items(o.getItems().stream().map(OrderItemResponse::from).toList())
                .status(o.getStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .deliveryZip(o.getDeliveryZip())
                .deliveryCity(o.getDeliveryCity())
                .deliveryMethod(o.getDeliveryMethod())
                .deliveryNotes(o.getDeliveryNotes())
                .subtotal(o.getSubtotal())
                .shippingCost(o.getShippingCost())
                .total(o.getTotal())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
