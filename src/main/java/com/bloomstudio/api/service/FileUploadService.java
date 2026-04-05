package com.bloomstudio.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service responsable de la sauvegarde des fichiers image sur le système de fichiers local.
 * Valide le type MIME, génère un nom unique par UUID et retourne le chemin public.
 */
@Service
public class FileUploadService {

    /** Extensions et types MIME acceptés pour les images produit */
    private static final List<String> TYPES_IMAGES_AUTORISES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    /** Répertoire de destination configuré dans application.yml */
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Sauvegarde un fichier image dans le répertoire d'upload.
     *
     * @param fichier le MultipartFile reçu dans la requête HTTP
     * @return l'URL publique relative du fichier sauvegardé (ex: /uploads/uuid.jpg)
     * @throws IllegalArgumentException si le fichier est vide ou n'est pas une image valide
     * @throws IOException              si une erreur d'écriture survient
     */
    public String sauvegarderImage(MultipartFile fichier) throws IOException {

        // Vérification : fichier non vide
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide ou absent.");
        }

        // Vérification du type MIME
        String typeMime = fichier.getContentType();
        if (typeMime == null || !TYPES_IMAGES_AUTORISES.contains(typeMime)) {
            throw new IllegalArgumentException(
                    "Type de fichier non supporté : " + typeMime
                    + ". Types acceptés : JPEG, PNG, WebP, GIF."
            );
        }

        // Extraction de l'extension à partir du nom original
        String nomOriginal = fichier.getOriginalFilename();
        String extension = extraireExtension(nomOriginal, typeMime);

        // Génération d'un nom unique pour éviter les collisions
        String nomFichier = UUID.randomUUID().toString() + extension;

        // Création du répertoire d'upload s'il n'existe pas encore
        Path cheminDossier = Paths.get(uploadDir);
        Files.createDirectories(cheminDossier);

        // Sauvegarde physique du fichier
        Path cheminDestination = cheminDossier.resolve(nomFichier);
        Files.copy(fichier.getInputStream(), cheminDestination, StandardCopyOption.REPLACE_EXISTING);

        // Retourne l'URL publique relative
        return "/uploads/" + nomFichier;
    }

    /**
     * Extrait l'extension du fichier à partir de son nom original.
     * Fallback sur le type MIME si le nom est absent ou sans extension.
     *
     * @param nomOriginal nom du fichier envoyé par le client
     * @param typeMime    type MIME détecté
     * @return l'extension avec le point (ex: ".jpg")
     */
    private String extraireExtension(String nomOriginal, String typeMime) {
        if (nomOriginal != null && nomOriginal.contains(".")) {
            String ext = nomOriginal.substring(nomOriginal.lastIndexOf(".")).toLowerCase();
            // On accepte uniquement les extensions cohérentes avec le type MIME
            if (List.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) {
                return ext;
            }
        }
        // Fallback basé sur le type MIME
        return switch (typeMime) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            default           -> ".jpg";
        };
    }
}
