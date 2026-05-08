package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.ProductRequest;
import com.bloomstudio.api.dto.response.ProductResponse;
import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.entity.ProductVariant;
import com.bloomstudio.api.enums.ProductCategory;
import com.bloomstudio.api.exception.BadRequestException;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.ProductRepository;
import com.bloomstudio.api.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    /**
     * Retourne tous les produits localisés selon la langue demandée.
     */
    public List<ProductResponse> getAll(String lang) {
        return productRepository.findAll().stream()
                .map(p -> ProductResponse.from(p, lang)).toList();
    }

    /**
     * Retourne les produits mis en avant, localisés selon la langue demandée.
     */
    public List<ProductResponse> getFeatured(String lang) {
        return productRepository.findByIsFeaturedTrue().stream()
                .map(p -> ProductResponse.from(p, lang)).toList();
    }

    /**
     * Retourne les nouveautés, localisées selon la langue demandée.
     */
    public List<ProductResponse> getNewArrivals(String lang) {
        return productRepository.findByIsNewTrue().stream()
                .map(p -> ProductResponse.from(p, lang)).toList();
    }

    /**
     * Retourne un produit par son identifiant, localisé selon la langue demandée.
     */
    public ProductResponse getById(Long id, String lang) {
        return ProductResponse.from(findById(id), lang);
    }

    /**
     * Retourne un produit par son slug, localisé selon la langue demandée.
     */
    public ProductResponse getBySlug(String slug, String lang) {
        return ProductResponse.from(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé : " + slug)), lang);
    }

    /**
     * Retourne les produits d'une catégorie, localisés selon la langue demandée.
     */
    public List<ProductResponse> getByCategory(ProductCategory category, String lang) {
        return productRepository.findByCategory(category).stream()
                .map(p -> ProductResponse.from(p, lang)).toList();
    }

    /**
     * Recherche plein texte, résultats localisés selon la langue demandée.
     */
    public List<ProductResponse> search(String query, String lang) {
        return productRepository.search(query).stream()
                .map(p -> ProductResponse.from(p, lang)).toList();
    }

    /**
     * Filtre les produits selon les critères fournis, résultats localisés selon la langue demandée.
     */
    public List<ProductResponse> filter(ProductCategory category, BigDecimal minPrice,
                                         BigDecimal maxPrice, Boolean inStock, String lang) {
        return productRepository.findWithFilters(category, inStock)
                .stream().map(p -> ProductResponse.from(p, lang)).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Un produit avec ce slug existe déjà");
        }
        Product product = toEntity(new Product(), request);
        product = productRepository.save(product);
        saveVariants(product, request);
        // Les endpoints admin retournent toujours la version française de référence
        return ProductResponse.from(productRepository.findById(product.getId()).orElseThrow(), "fr");
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        if (!product.getSlug().equals(request.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Un produit avec ce slug existe déjà");
        }
        product = toEntity(product, request);
        product = productRepository.save(product);
        variantRepository.deleteAll(variantRepository.findByProductId(product.getId()));
        saveVariants(product, request);
        // Les endpoints admin retournent toujours la version française de référence
        return ProductResponse.from(productRepository.findById(product.getId()).orElseThrow(), "fr");
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findById(id));
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé : " + id));
    }

    private Product toEntity(Product product, ProductRequest r) {
        product.setName(r.getName());
        product.setNameEn(r.getNameEn());
        product.setNameNl(r.getNameNl());
        product.setSlug(r.getSlug());
        product.setDescription(r.getDescription());
        product.setDescriptionEn(r.getDescriptionEn());
        product.setDescriptionNl(r.getDescriptionNl());
        product.setShortDescription(r.getShortDescription());
        product.setShortDescriptionEn(r.getShortDescriptionEn());
        product.setShortDescriptionNl(r.getShortDescriptionNl());
        product.setImages(r.getImages());
        product.setCategory(r.getCategory());
        product.setTags(r.getTags());
        product.setIsNew(Boolean.TRUE.equals(r.getIsNew()));
        product.setIsFeatured(Boolean.TRUE.equals(r.getIsFeatured()));
        product.setIsSeasonal(Boolean.TRUE.equals(r.getIsSeasonal()));
        product.setInStock(Boolean.TRUE.equals(r.getInStock()));
        return product;
    }

    private void saveVariants(Product product, ProductRequest request) {
        if (request.getVariants() == null) return;
        for (ProductRequest.VariantRequest vr : request.getVariants()) {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .size(vr.getSize())
                    .price(vr.getPrice())
                    .build();
            variantRepository.save(variant);
        }
    }
}
