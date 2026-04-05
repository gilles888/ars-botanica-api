package com.bloomstudio.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse retourné lors de la vérification du statut d'une session Stripe.
 * Utilisé par la page de confirmation pour connaître le résultat du paiement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusResponse {

    /**
     * Statut de la session Stripe : "open", "complete" ou "expired".
     */
    private String status;

    /**
     * Numéro de commande Ars Botanica associé à la session.
     * Extrait des métadonnées Stripe.
     */
    private String orderNumber;

    /**
     * Email du client extrait des customerDetails de la session Stripe.
     * Disponible uniquement lorsque le paiement est complété.
     */
    private String customerEmail;
}
