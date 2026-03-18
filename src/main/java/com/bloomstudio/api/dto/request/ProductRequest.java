package com.bloomstudio.api.dto.request;

import com.bloomstudio.api.enums.ProductCategory;
import com.bloomstudio.api.enums.ProductSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "Le slug est obligatoire")
    private String slug;

    private String description;
    private String shortDescription;

    private List<String> images;

    @NotNull(message = "La catégorie est obligatoire")
    private ProductCategory category;

    private List<String> tags;

    private Boolean isNew      = false;
    private Boolean isFeatured = false;
    private Boolean isSeasonal = false;
    private Boolean inStock    = true;

    @Data
    public static class VariantRequest {
        @NotNull
        private ProductSize size;
        @NotNull
        private java.math.BigDecimal price;
    }

    @NotEmpty(message = "Les variantes sont obligatoires")
    private List<VariantRequest> variants;
}
