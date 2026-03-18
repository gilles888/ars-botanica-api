package com.bloomstudio.api.dto.response;

import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.enums.ProductCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private List<String> images;
    private ProductCategory category;
    private List<String> tags;
    private Double rating;
    private Integer reviewCount;
    private Boolean isNew;
    private Boolean isFeatured;
    private Boolean isSeasonal;
    private Boolean inStock;
    private LocalDateTime createdAt;
    private List<ProductVariantResponse> variants;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .shortDescription(p.getShortDescription())
                .images(p.getImages())
                .category(p.getCategory())
                .tags(p.getTags())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .isNew(p.getIsNew())
                .isFeatured(p.getIsFeatured())
                .isSeasonal(p.getIsSeasonal())
                .inStock(p.getInStock())
                .createdAt(p.getCreatedAt())
                .variants(p.getVariants().stream().map(ProductVariantResponse::from).collect(Collectors.toList()))
                .build();
    }
}
