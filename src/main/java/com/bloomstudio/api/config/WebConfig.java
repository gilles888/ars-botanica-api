package com.bloomstudio.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration MVC pour exposer les fichiers du répertoire d'upload
 * en tant que ressources statiques accessibles via l'URL /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Répertoire physique des uploads, configuré dans application.yml */
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Mappe les requêtes GET /uploads/** vers le répertoire local d'upload.
     * Exemple : GET /uploads/abc123.jpg sert le fichier /home/…/uploads/abc123.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // S'assurer que le chemin se termine par un slash pour Spring MVC
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + location);
    }
}
