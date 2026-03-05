package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.ProductRequest;
import com.bloomstudio.api.dto.response.ProductResponse;
import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.enums.ProductCategory;
import com.bloomstudio.api.exception.BadRequestException;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getFeatured() {
        return productRepository.findByIsFeaturedTrue().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getNewArrivals() {
        return productRepository.findByIsNewTrue().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findById(id));
    }

    public ProductResponse getBySlug(String slug) {
        return ProductResponse.from(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé : " + slug)));
    }

    public List<ProductResponse> getByCategory(ProductCategory category) {
        return productRepository.findByCategory(category).stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> search(String query) {
        return productRepository.search(query).stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> filter(ProductCategory category, BigDecimal minPrice,
                                         BigDecimal maxPrice, Boolean inStock) {
        return productRepository.findWithFilters(category, minPrice, maxPrice, inStock)
                .stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Un produit avec ce slug existe déjà");
        }
        Product product = toEntity(new Product(), request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        if (!product.getSlug().equals(request.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Un produit avec ce slug existe déjà");
        }
        return ProductResponse.from(productRepository.save(toEntity(product, request)));
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
        product.setSlug(r.getSlug());
        product.setDescription(r.getDescription());
        product.setShortDescription(r.getShortDescription());
        product.setPrice(r.getPrice());
        product.setOriginalPrice(r.getOriginalPrice());
        product.setImages(r.getImages());
        product.setCategory(r.getCategory());
        product.setTags(r.getTags());
        product.setIsNew(Boolean.TRUE.equals(r.getIsNew()));
        product.setIsFeatured(Boolean.TRUE.equals(r.getIsFeatured()));
        product.setIsSeasonal(Boolean.TRUE.equals(r.getIsSeasonal()));
        product.setInStock(Boolean.TRUE.equals(r.getInStock()));
        return product;
    }
}
