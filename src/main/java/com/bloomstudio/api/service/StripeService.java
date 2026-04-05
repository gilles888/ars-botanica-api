package com.bloomstudio.api.service;

import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.entity.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable de toutes les interactions avec l'API Stripe.
 * Gère la création des sessions Checkout Embedded et la récupération des sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${app.stripe.secret-key}")
    private String secretKey;

    @Value("${app.stripe.return-url}")
    private String returnUrl;

    /**
     * Initialise la clé secrète Stripe au démarrage de l'application.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe initialisé avec la clé : {}***", secretKey.substring(0, 12));
    }

    /**
     * Crée une session Stripe Checkout en mode Embedded pour une commande donnée.
     *
     * @param order la commande Ars Botanica contenant les articles et le total
     * @return la Session Stripe créée, contenant le clientSecret et l'id de session
     * @throws StripeException en cas d'erreur lors de l'appel à l'API Stripe
     */
    public Session createCheckoutSession(Order order) throws StripeException {
        log.info("Création de la session Stripe pour la commande {}", order.getOrderNumber());

        // Construction des line items Stripe depuis les OrderItems
        List<SessionCreateParams.LineItem> lineItems = buildLineItems(order);

        // Ajout des frais de livraison comme line item si non nuls
        if (order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            lineItems.add(buildShippingLineItem(order.getShippingCost()));
        }

        // Construction des paramètres de session en mode Embedded
        SessionCreateParams params = SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                // L'URL de retour reçoit automatiquement {CHECKOUT_SESSION_ID} substitué par Stripe
                .setReturnUrl(returnUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCurrency("eur")
                .addAllLineItem(lineItems)
                // Métadonnées pour retrouver la commande côté backend lors du webhook
                .putMetadata("orderNumber", order.getOrderNumber())
                .putMetadata("orderId", String.valueOf(order.getId()))
                .build();

        Session session = Session.create(params);
        log.info("Session Stripe créée : {} pour la commande {}", session.getId(), order.getOrderNumber());
        return session;
    }

    /**
     * Récupère une session Stripe existante par son identifiant.
     *
     * @param sessionId l'identifiant de la session Stripe (cs_xxx)
     * @return la Session Stripe correspondante
     * @throws StripeException en cas d'erreur lors de l'appel à l'API Stripe
     */
    public Session retrieveSession(String sessionId) throws StripeException {
        log.info("Récupération de la session Stripe : {}", sessionId);
        return Session.retrieve(sessionId);
    }

    /**
     * Construit la liste des line items Stripe à partir des articles de la commande.
     * Les montants sont convertis en centimes (obligatoire pour Stripe).
     */
    private List<SessionCreateParams.LineItem> buildLineItems(Order order) {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            // Conversion du prix en centimes (Stripe attend des entiers)
            long unitAmountCents = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            // Nom de l'article : "Nom du produit - Taille XXL" par exemple
            String productName = item.getProduct().getName();
            if (item.getSize() != null) {
                productName = productName + " - " + item.getSize().name();
            }

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(unitAmountCents)
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(productName)
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            lineItems.add(lineItem);
        }

        return lineItems;
    }

    /**
     * Construit le line item Stripe pour les frais de livraison.
     *
     * @param shippingCost le montant des frais de livraison
     * @return le line item Stripe correspondant aux frais de livraison
     */
    private SessionCreateParams.LineItem buildShippingLineItem(BigDecimal shippingCost) {
        long shippingCents = shippingCost
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(shippingCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Frais de livraison")
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
