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
    // Traductions du nom
    private String nameEn;
    private String nameNl;
    private String slug;
    private String description;
    // Traductions de la description complète
    private String descriptionEn;
    private String descriptionNl;
    private String shortDescription;
    // Traductions de la description courte
    private String shortDescriptionEn;
    private String shortDescriptionNl;
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
                .nameEn(p.getNameEn())
                .nameNl(p.getNameNl())
                .slug(p.getSlug())
                .description(p.getDescription())
                .descriptionEn(p.getDescriptionEn())
                .descriptionNl(p.getDescriptionNl())
                .shortDescription(p.getShortDescription())
                .shortDescriptionEn(p.getShortDescriptionEn())
                .shortDescriptionNl(p.getShortDescriptionNl())
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
