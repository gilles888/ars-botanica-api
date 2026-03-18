package com.bloomstudio.api.config;

import com.bloomstudio.api.entity.Product;
import com.bloomstudio.api.entity.ProductVariant;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.ProductCategory;
import com.bloomstudio.api.enums.ProductSize;
import com.bloomstudio.api.enums.Role;
import com.bloomstudio.api.repository.ProductRepository;
import com.bloomstudio.api.repository.ProductVariantRepository;
import com.bloomstudio.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // ── Users ──────────────────────────────────────────────
            if (userRepository.count() == 0) {
                userRepository.saveAll(List.of(
                    User.builder()
                        .firstName("Admin")
                        .lastName("Bloom")
                        .email("admin@bloom-studio.fr")
                        .password(passwordEncoder.encode("admin1234"))
                        .role(Role.ADMIN)
                        .build(),
                    User.builder()
                        .firstName("Marie")
                        .lastName("Dupont")
                        .email("marie@exemple.fr")
                        .password(passwordEncoder.encode("client1234"))
                        .phone("+33600000001")
                        .role(Role.CLIENT)
                        .build()
                ));
                log.info("✅ Utilisateurs créés (admin@bloom-studio.fr / admin1234)");
            }

            // ── Products ───────────────────────────────────────────
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                    Product.builder()
                        .name("Bouquet Jardin de Roses")
                        .slug("bouquet-jardin-roses")
                        .description("Un bouquet généreux de roses fraîches dans les tons roses et blancs.")
                        .shortDescription("Roses fraîches en tons pastels avec feuillage vert")
                        .images(List.of("https://images.unsplash.com/photo-1487530811176-3780de880c2d?w=600"))
                        .category(ProductCategory.BOUQUETS)
                        .tags(List.of("roses", "anniversaire", "romantique"))
                        .rating(4.8).reviewCount(124)
                        .isNew(false).isFeatured(true).isSeasonal(false).inStock(true)
                        .build(),
                    Product.builder()
                        .name("Composition Printemps Éternel")
                        .slug("composition-printemps-eternel")
                        .description("Tulipes, renoncules et marguerites dans un vase en céramique fait main.")
                        .shortDescription("Tulipes, renoncules et marguerites en vase céramique")
                        .images(List.of("https://images.unsplash.com/photo-1559563362-c667ba5f5480?w=600"))
                        .category(ProductCategory.COMPOSITIONS)
                        .tags(List.of("printemps", "tulipes", "vase"))
                        .rating(4.9).reviewCount(87)
                        .isNew(false).isFeatured(true).isSeasonal(true).inStock(true)
                        .build(),
                    Product.builder()
                        .name("Orchidée Phalaenopsis")
                        .slug("orchidee-phalaenopsis")
                        .description("Une orchidée phalaenopsis élégante en pot décoratif.")
                        .shortDescription("Orchidée en pot décoratif, longue durée de vie")
                        .images(List.of("https://images.unsplash.com/photo-1566907225472-514f4c9a16bf?w=600"))
                        .category(ProductCategory.PLANTES)
                        .tags(List.of("orchidée", "intérieur", "élégant"))
                        .rating(4.7).reviewCount(203)
                        .isNew(false).isFeatured(true).isSeasonal(false).inStock(true)
                        .build(),
                    Product.builder()
                        .name("Bouquet de Mariée Intemporel")
                        .slug("bouquet-mariee-intemporel")
                        .description("Pivoines blanches, roses garden et feuilles de magnolia.")
                        .shortDescription("Pivoines blanches et roses garden pour votre mariage")
                        .images(List.of("https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=600"))
                        .category(ProductCategory.MARIAGES)
                        .tags(List.of("mariage", "mariée", "pivoines", "luxe"))
                        .rating(5.0).reviewCount(56)
                        .isNew(false).isFeatured(true).isSeasonal(false).inStock(true)
                        .build(),
                    Product.builder()
                        .name("Bouquet Sauvage & Naturel")
                        .slug("bouquet-sauvage-naturel")
                        .description("Un bouquet champêtre mêlant fleurs sauvages et herbes aromatiques.")
                        .shortDescription("Fleurs sauvages et herbes dans un esprit champêtre")
                        .images(List.of("https://images.unsplash.com/photo-1468327768560-75b778cbb551?w=600"))
                        .category(ProductCategory.BOUQUETS)
                        .tags(List.of("champêtre", "naturel", "bohème"))
                        .rating(4.6).reviewCount(91)
                        .isNew(true).isFeatured(false).isSeasonal(false).inStock(true)
                        .build(),
                    Product.builder()
                        .name("Succulentes en Jardinière")
                        .slug("succulentes-jardiniere")
                        .description("Assortiment de succulentes colorées dans une jardinière en bois naturel.")
                        .shortDescription("Mix de succulentes en jardinière bois")
                        .images(List.of("https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"))
                        .category(ProductCategory.PLANTES)
                        .tags(List.of("succulentes", "jardinière", "bureau"))
                        .rating(4.5).reviewCount(178)
                        .isNew(true).isFeatured(false).isSeasonal(false).inStock(true)
                        .build()
                ));
                log.info("✅ {} produits créés", productRepository.count());

                // ── Product Variants ───────────────────────────────
                saveVariants("bouquet-jardin-roses",
                    new BigDecimal("45.00"), new BigDecimal("65.00"), new BigDecimal("90.00"));
                saveVariants("composition-printemps-eternel",
                    new BigDecimal("62.00"), new BigDecimal("89.00"), new BigDecimal("123.00"));
                saveVariants("orchidee-phalaenopsis",
                    new BigDecimal("31.00"), new BigDecimal("45.00"), new BigDecimal("62.00"));
                saveVariants("bouquet-mariee-intemporel",
                    new BigDecimal("126.00"), new BigDecimal("180.00"), new BigDecimal("248.00"));
                saveVariants("bouquet-sauvage-naturel",
                    new BigDecimal("34.00"), new BigDecimal("48.00"), new BigDecimal("66.00"));
                saveVariants("succulentes-jardiniere",
                    new BigDecimal("27.00"), new BigDecimal("38.00"), new BigDecimal("52.00"));

                log.info("✅ Variantes de produits créées");
            }
        };
    }

    private void saveVariants(String slug, BigDecimal petit, BigDecimal moyen, BigDecimal grand) {
        productRepository.findBySlug(slug).ifPresent(product -> {
            variantRepository.saveAll(List.of(
                ProductVariant.builder().product(product).size(ProductSize.PETIT).price(petit).build(),
                ProductVariant.builder().product(product).size(ProductSize.MOYEN).price(moyen).build(),
                ProductVariant.builder().product(product).size(ProductSize.GRAND).price(grand).build()
            ));
        });
    }
}
