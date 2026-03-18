package com.bloomstudio.api.repository;

import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByIsFeaturedTrue();

    List<Product> findByIsNewTrue();

    List<Product> findByInStockTrue();

    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:inStock IS NULL OR p.inStock = :inStock)")
    List<Product> findWithFilters(
            @Param("category") ProductCategory category,
            @Param("inStock") Boolean inStock
    );

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> search(@Param("query") String query);

    boolean existsBySlug(String slug);
}
