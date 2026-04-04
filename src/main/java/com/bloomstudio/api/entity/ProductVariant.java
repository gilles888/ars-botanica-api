package com.bloomstudio.api.entity;

import com.bloomstudio.api.enums.ProductSize;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Variante d'un produit floral (taille + prix associé).
 */
@Entity
@Table(name = "product_variants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSize size;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
