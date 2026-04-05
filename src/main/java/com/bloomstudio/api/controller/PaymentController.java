package com.bloomstudio.api.controller;

import com.bloomstudio.api.dto.request.OrderRequest;
import com.bloomstudio.api.dto.response.CheckoutResponse;
import com.bloomstudio.api.dto.response.SessionStatusResponse;
import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.service.OrderService;
import com.bloomstudio.api.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Contrôleur gérant l'intégration Stripe Embedded Checkout.
 * Expose trois endpoints : création de session, webhook Stripe et vérification de statut.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements")
public class PaymentController {

    private final OrderService orderService;
    private final StripeService stripeService;

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    // ----------------------------------------------------------------
    // POST /api/payments/checkout
    // ----------------------------------------------------------------

    /**
     * Crée une commande et une session Stripe Embedded Checkout.
     * Retourne le clientSecret nécessaire au composant Stripe côté frontend.
     *
     * @param request  le corps de la requête contenant les articles et les informations de livraison
     * @param userDetails l'utilisateur authentifié extrait du JWT
     * @return CheckoutResponse contenant clientSecret, sessionId et orderNumber
     */
    @PostMapping("/checkout")
    @Operation(summary = "Créer une session Stripe Embedded Checkout")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Création d'une session checkout pour l'utilisateur : {}", userDetails.getUsername());

        // 1. Créer la commande en base avec le statut PAYMENT_PENDING
        Order order = orderService.createOrderEntity(request, userDetails.getUsername());
        log.info("Commande créée : {} (id={})", order.getOrderNumber(), order.getId());

        try {
            // 2. Créer la session Stripe Embedded Checkout
            Session session = stripeService.createCheckoutSession(order);

            // 3. Persister l'identifiant de session Stripe dans la commande
            orderService.updateStripeSession(order.getId(), session.getId());

            // 4. Retourner le clientSecret et les infos nécessaires au frontend
            CheckoutResponse response = CheckoutResponse.builder()
                    .clientSecret(session.getClientSecret())
                    .sessionId(session.getId())
                    .orderNumber(order.getOrderNumber())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (StripeException e) {
            // En cas d'échec Stripe, la commande reste en PAYMENT_PENDING
            // Le frontend devra réessayer ou contacter le support
            log.error("Erreur Stripe lors de la création de la session pour la commande {} : {}",
                    order.getOrderNumber(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création de la session de paiement : " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------
    // POST /api/payments/webhook (public — vérifié par signature Stripe)
    // ----------------------------------------------------------------

    /**
     * Endpoint Stripe Webhook : reçoit les événements asynchrones de Stripe.
     * Cet endpoint est public car Stripe l'appelle directement (sans JWT).
     * La sécurité est assurée par la vérification de la signature Stripe.
     *
     * @param request la requête HTTP brute (nécessaire pour lire le payload exact)
     * @return 200 OK si l'événement est traité, 400 si la signature est invalide
     */
    @PostMapping("/webhook")
    @Operation(summary = "Webhook Stripe (public)", description = "Reçoit les événements Stripe. Accès public, sécurisé par signature.")
    public ResponseEntity<Map<String, String>> handleWebhook(HttpServletRequest request) {
        String payload;
        try {
            // Lecture du corps brut de la requête — obligatoire pour valider la signature Stripe
            payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Impossible de lire le payload du webhook Stripe : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Payload illisible"));
        }

        String sigHeader = request.getHeader("Stripe-Signature");

        // Vérification de la signature Stripe si le webhook-secret est configuré
        Event event;
        if (StringUtils.hasText(webhookSecret)) {
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
                log.info("Signature Stripe vérifiée avec succès pour l'événement : {}", event.getType());
            } catch (SignatureVerificationException e) {
                log.warn("Signature Stripe invalide : {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("error", "Signature invalide"));
            }
        } else {
            // Pas de webhook-secret configuré (développement local) : on parse sans vérifier
            log.warn("STRIPE_WEBHOOK_SECRET non configuré — signature non vérifiée (mode dev uniquement)");
            try {
                event = Event.GSON.fromJson(payload, Event.class);
            } catch (Exception e) {
                log.error("Impossible de parser le payload Stripe : {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("error", "Payload invalide"));
            }
        }

        // Traitement des événements Stripe
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                // La session est complète : le paiement a été accepté par Stripe
                event.getDataObjectDeserializer()
                        .getObject()
                        .ifPresent(stripeObject -> {
                            Session session = (Session) stripeObject;
                            log.info("Paiement confirmé pour la session Stripe : {}", session.getId());
                            // Passage de la commande au statut PAID
                            orderService.confirmPayment(session.getId());
                        });
            }
            case "checkout.session.expired" -> {
                // La session a expiré sans paiement : on peut loguer mais la commande reste PAYMENT_PENDING
                event.getDataObjectDeserializer()
                        .getObject()
                        .ifPresent(stripeObject -> {
                            Session session = (Session) stripeObject;
                            log.info("Session Stripe expirée : {}", session.getId());
                        });
            }
            default -> log.debug("Événement Stripe ignoré : {}", event.getType());
        }

        return ResponseEntity.ok(Map.of("received", "true"));
    }

    // ----------------------------------------------------------------
    // GET /api/payments/session/{sessionId}
    // ----------------------------------------------------------------

    /**
     * Vérifie le statut d'une session Stripe par son identifiant.
     * Si le paiement est confirmé ("paid") et que la commande n'est pas encore PAID,
     * la commande est immédiatement passée au statut PAID (filet de sécurité en cas
     * de non-réception du webhook Stripe).
     *
     * @param sessionId l'identifiant de la session Stripe (cs_xxx)
     * @return SessionStatusResponse contenant le statut, le numéro de commande et l'email client
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Vérifier le statut d'une session Stripe")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(@PathVariable String sessionId) {
        log.info("Vérification du statut de la session Stripe : {}", sessionId);

        try {
            Session session = stripeService.retrieveSession(sessionId);

            // Récupération du numéro de commande depuis les métadonnées Stripe
            String orderNumber = session.getMetadata() != null
                    ? session.getMetadata().get("orderNumber")
                    : null;

            // Récupération de l'email client depuis les customerDetails Stripe
            String customerEmail = session.getCustomerDetails() != null
                    ? session.getCustomerDetails().getEmail()
                    : null;

            // Filet de sécurité : si Stripe confirme le paiement mais que le webhook
            // n'a pas encore été reçu, on confirme manuellement la commande ici.
            if ("paid".equals(session.getPaymentStatus()) && !orderService.isAlreadyPaid(sessionId)) {
                log.info("Paiement confirmé via polling pour la session {} — confirmation manuelle de la commande {}",
                        sessionId, orderNumber);
                orderService.confirmPayment(sessionId);
            }

            SessionStatusResponse response = SessionStatusResponse.builder()
                    .status(session.getStatus())
                    .orderNumber(orderNumber)
                    .customerEmail(customerEmail)
                    .build();

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Erreur lors de la récupération de la session Stripe {} : {}", sessionId, e.getMessage());
            throw new RuntimeException("Session de paiement introuvable", e);
        }
    }
}
