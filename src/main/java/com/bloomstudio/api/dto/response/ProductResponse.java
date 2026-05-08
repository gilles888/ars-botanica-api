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
    // Champ localisé selon Accept-Language (fr/en/nl)
    private String name;
    private String slug;
    // Champ localisé selon Accept-Language (fr/en/nl)
    private String description;
    // Champ localisé selon Accept-Language (fr/en/nl)
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

    /**
     * Résout la valeur localisée d'un champ texte selon la langue demandée.
     * Si la traduction est absente ou vide, on retombe sur la valeur française.
     *
     * @param fr    valeur en français (valeur de référence)
     * @param en    valeur en anglais (peut être null)
     * @param nl    valeur en néerlandais (peut être null)
     * @param lang  code langue : "en", "nl" ou autre (→ fr par défaut)
     * @return      la valeur localisée résolue
     */
    private static String resoudre(String fr, String en, String nl, String lang) {
        if ("en".equalsIgnoreCase(lang) && en != null && !en.isBlank()) {
            return en;
        }
        if ("nl".equalsIgnoreCase(lang) && nl != null && !nl.isBlank()) {
            return nl;
        }
        return fr;
    }

    /**
     * Construit un ProductResponse localisé selon la langue fournie.
     * Seuls les champs name, description et shortDescription sont traduits ;
     * tous les autres champs restent inchangés.
     *
     * @param p     l'entité produit
     * @param lang  le code langue extrait du header Accept-Language ("fr", "en", "nl")
     * @return      le DTO prêt à être sérialisé
     */
    public static ProductResponse from(Product p, String lang) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(resoudre(p.getName(), p.getNameEn(), p.getNameNl(), lang))
                .slug(p.getSlug())
                .description(resoudre(p.getDescription(), p.getDescriptionEn(), p.getDescriptionNl(), lang))
                .shortDescription(resoudre(p.getShortDescription(), p.getShortDescriptionEn(), p.getShortDescriptionNl(), lang))
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
