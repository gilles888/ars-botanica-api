package com.bloomstudio.api.dto.request;

import com.bloomstudio.api.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "Le slug est obligatoire")
    private String slug;

    private String description;
    private String shortDescription;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être positif")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private List<String> images;

    @NotNull(message = "La catégorie est obligatoire")
    private ProductCategory category;

    private List<String> tags;

    private Boolean isNew      = false;
    private Boolean isFeatured = false;
    private Boolean isSeasonal = false;
    private Boolean inStock    = true;
}
