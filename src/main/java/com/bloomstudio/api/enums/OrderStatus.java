package com.bloomstudio.api.enums;

public enum OrderStatus {
    PAYMENT_PENDING,   // commande créée, paiement Stripe en attente
    PAID,              // paiement reçu (webhook Stripe)
    PENDING,
    CONFIRMED,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
