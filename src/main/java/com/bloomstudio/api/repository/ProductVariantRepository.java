package com.bloomstudio.api.repository;

import com.bloomstudio.api.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour les variantes de produits.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Retourne toutes les variantes d'un produit donné.
     */
    List<ProductVariant> findByProductId(Long productId);
}
