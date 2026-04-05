package com.bloomstudio.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse retourné au frontend lors de la création d'une session Stripe Embedded Checkout.
 * Le frontend utilise le clientSecret pour initialiser le composant Stripe Embedded Checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

    /**
     * Secret client Stripe nécessaire pour initialiser le composant Embedded Checkout côté frontend.
     * Format : cs_xxx_secret_yyy
     */
    private String clientSecret;

    /**
     * Identifiant de la session Stripe (cs_xxx).
     * Utilisé par le frontend pour vérifier le statut après le paiement.
     */
    private String sessionId;

    /**
     * Numéro de commande Ars Botanica (ex : BS-A1B2C3D4).
     * Affiché sur la page de confirmation.
     */
    private String orderNumber;
}
