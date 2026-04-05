package com.bloomstudio.api.controller;

import com.bloomstudio.api.dto.request.ProductRequest;
import com.bloomstudio.api.dto.response.ProductResponse;
import com.bloomstudio.api.enums.ProductCategory;
import com.bloomstudio.api.service.FileUploadService;
import com.bloomstudio.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produits")
public class ProductController {

    private final ProductService productService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @Operation(summary = "Lister tous les produits")
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/featured")
    @Operation(summary = "Produits mis en avant")
    public ResponseEntity<List<ProductResponse>> getFeatured() {
        return ResponseEntity.ok(productService.getFeatured());
    }

    @GetMapping("/new")
    @Operation(summary = "Nouveautés")
    public ResponseEntity<List<ProductResponse>> getNew() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail par ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Détail par slug")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Produits par catégorie")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable ProductCategory category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche plein texte")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(productService.search(q));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer les produits")
    public ResponseEntity<List<ProductResponse>> filter(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock) {
        return ResponseEntity.ok(productService.filter(category, minPrice, maxPrice, inStock));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un produit", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un produit", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un produit", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload d'une image pour un produit.
     * Réservé aux administrateurs. Le fichier est sauvegardé dans le répertoire
     * d'upload et l'URL publique est retournée dans le champ "url".
     *
     * @param file le fichier image envoyé en multipart/form-data (champ "file")
     * @return 200 OK avec {"url": "/uploads/nom-fichier.ext"}
     *         400 BAD REQUEST si le type n'est pas supporté ou si le fichier est vide
     *         500 INTERNAL SERVER ERROR en cas d'erreur d'écriture
     */
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Uploader une image produit",
        description = "Accepte un fichier image (JPEG, PNG, WebP, GIF) jusqu'à 10 Mo. "
                    + "Retourne l'URL publique à stocker dans le champ images du produit.",
        security = @SecurityRequirement(name = "Bearer Auth")
    )
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {
            String url = fileUploadService.sauvegarderImage(file);
            // Retourne {"url": "/uploads/abc123.jpg"}
            return ResponseEntity.ok(Map.of("url", url));

        } catch (IllegalArgumentException e) {
            // Type non supporté ou fichier vide
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IOException e) {
            // Erreur d'écriture sur le disque
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la sauvegarde du fichier : " + e.getMessage()));
        }
    }
}
