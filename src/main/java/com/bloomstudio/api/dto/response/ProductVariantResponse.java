package com.bloomstudio.api.dto.response;

import com.bloomstudio.api.entity.ProductVariant;
import com.bloomstudio.api.enums.ProductSize;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de réponse pour une variante de produit (taille + prix).
 */
@Data
@Builder
public class ProductVariantResponse {

    private Long id;
    private ProductSize size;
    private BigDecimal price;

    public static ProductVariantResponse from(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .size(v.getSize())
                .price(v.getPrice())
                .build();
    }
}
