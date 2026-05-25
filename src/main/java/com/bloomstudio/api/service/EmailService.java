package com.bloomstudio.api.service;

import com.bloomstudio.api.entity.Order;
import com.bloomstudio.api.entity.OrderItem;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Bienvenue chez Ars Botanica 🌿";
        String body = buildWelcomeHtml(user);
        send(user.getEmail(), subject, body);
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        String subject = "Confirmation de votre commande " + order.getOrderNumber();
        String body = buildOrderConfirmationHtml(order);
        send(order.getUser().getEmail(), subject, body);
    }

    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        String subject = "Votre commande " + order.getOrderNumber() + " — " + statusLabel(order.getStatus());
        String body = buildStatusUpdateHtml(order);
        send(order.getUser().getEmail(), subject, body);
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Réinitialisation de votre mot de passe";
        String body = buildPasswordResetHtml(user, token);
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email envoyé à {} — {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Échec envoi email à {} : {}", to, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Templates HTML
    // -------------------------------------------------------------------------

    private String buildWelcomeHtml(User user) {
        return wrap("""
            <h2 style="color:#2d6a4f;">Bienvenue, %s !</h2>
            <p>Votre compte Ars Botanica a bien été créé.</p>
            <p>Vous pouvez dès maintenant explorer notre collection de plantes et passer commande.</p>
            <p style="margin-top:24px;">
              <a href="%s" style="%s">Visiter la boutique</a>
            </p>
            <p>À bientôt chez Ars Botanica 🌿</p>
            """.formatted(user.getFirstName(), frontendUrl, btnStyle()));
    }

    private String buildOrderConfirmationHtml(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("<tr>")
                 .append("<td style='padding:6px 8px;border-bottom:1px solid #e8f5e9;'>")
                 .append(item.getProduct().getName())
                 .append(item.getSize() != null ? " (" + item.getSize() + ")" : "")
                 .append("</td>")
                 .append("<td style='padding:6px 8px;border-bottom:1px solid #e8f5e9;text-align:center;'>")
                 .append(item.getQuantity())
                 .append("</td>")
                 .append("<td style='padding:6px 8px;border-bottom:1px solid #e8f5e9;text-align:right;'>")
                 .append(formatEur(item.getSubtotal()))
                 .append("</td>")
                 .append("</tr>");
        }

        return wrap("""
            <h2 style="color:#2d6a4f;">Merci pour votre commande !</h2>
            <p>Votre paiement a été confirmé. Voici le récapitulatif de votre commande <strong>%s</strong>.</p>
            <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
              <thead>
                <tr style="background:#e8f5e9;">
                  <th style="padding:8px;text-align:left;">Produit</th>
                  <th style="padding:8px;text-align:center;">Qté</th>
                  <th style="padding:8px;text-align:right;">Total</th>
                </tr>
              </thead>
              <tbody>%s</tbody>
              <tfoot>
                <tr><td colspan="2" style="padding:6px 8px;text-align:right;font-weight:bold;">Sous-total</td>
                    <td style="padding:6px 8px;text-align:right;">%s</td></tr>
                <tr><td colspan="2" style="padding:6px 8px;text-align:right;font-weight:bold;">Livraison</td>
                    <td style="padding:6px 8px;text-align:right;">%s</td></tr>
                <tr style="background:#e8f5e9;font-size:1.1em;">
                    <td colspan="2" style="padding:8px;text-align:right;font-weight:bold;">Total</td>
                    <td style="padding:8px;text-align:right;font-weight:bold;">%s</td></tr>
              </tfoot>
            </table>
            <p><strong>Livraison à :</strong> %s, %s %s</p>
            <p>Nous vous informerons dès que votre commande sera expédiée.</p>
            """.formatted(
                order.getOrderNumber(),
                items,
                formatEur(order.getSubtotal()),
                order.getShippingCost().compareTo(BigDecimal.ZERO) == 0 ? "Offerte" : formatEur(order.getShippingCost()),
                formatEur(order.getTotal()),
                order.getDeliveryAddress(),
                order.getDeliveryZip(),
                order.getDeliveryCity()
        ));
    }

    private String buildStatusUpdateHtml(Order order) {
        String statusMsg = switch (order.getStatus()) {
            case CONFIRMED  -> "Votre commande a été confirmée et sera bientôt préparée.";
            case PREPARING  -> "Votre commande est en cours de préparation.";
            case SHIPPED    -> buildShippedMessage(order);
            case DELIVERED  -> "Votre commande a été livrée. Nous espérons que vous êtes satisfait(e) !";
            case CANCELLED  -> "Votre commande a été annulée. Si vous avez des questions, contactez-nous.";
            default         -> "Le statut de votre commande a été mis à jour.";
        };

        return wrap("""
            <h2 style="color:#2d6a4f;">Mise à jour de votre commande</h2>
            <p>Commande <strong>%s</strong> — statut : <strong>%s</strong></p>
            <p>%s</p>
            <p style="margin-top:24px;">
              <a href="%s/compte/commandes" style="%s">Voir mes commandes</a>
            </p>
            """.formatted(
                order.getOrderNumber(),
                statusLabel(order.getStatus()),
                statusMsg,
                frontendUrl,
                btnStyle()
        ));
    }

    private String buildShippedMessage(Order order) {
        StringBuilder sb = new StringBuilder("Votre commande est en route !");
        if (order.getCarrier() != null) sb.append(" Transporteur : <strong>").append(order.getCarrier()).append("</strong>.");
        if (order.getTrackingNumber() != null) sb.append(" Numéro de suivi : <strong>").append(order.getTrackingNumber()).append("</strong>.");
        return sb.toString();
    }

    private String buildPasswordResetHtml(User user, String token) {
        String link = frontendUrl + "/reinitialiser-mot-de-passe?token=" + token;
        return wrap("""
            <h2 style="color:#2d6a4f;">Réinitialisation de votre mot de passe</h2>
            <p>Bonjour %s,</p>
            <p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous (valable 1 heure).</p>
            <p style="margin-top:24px;">
              <a href="%s" style="%s">Réinitialiser mon mot de passe</a>
            </p>
            <p style="color:#888;font-size:0.9em;">Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
            """.formatted(user.getFirstName(), link, btnStyle()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String wrap(String content) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;background:#f9f9f9;margin:0;padding:0;">
              <div style="max-width:600px;margin:32px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                <div style="background:#2d6a4f;padding:24px 32px;">
                  <h1 style="color:#fff;margin:0;font-size:1.4em;letter-spacing:.5px;">ARS BOTANICA</h1>
                </div>
                <div style="padding:32px;">
                  %s
                  <hr style="border:none;border-top:1px solid #e8f5e9;margin:32px 0;">
                  <p style="color:#888;font-size:0.85em;margin:0;">
                    Ars Botanica · arsbotanica@outlook.com<br>
                    Vous recevez cet email car vous avez un compte sur arsbotanica.gilmotech.be
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(content);
    }

    private String btnStyle() {
        return "display:inline-block;background:#2d6a4f;color:#fff;padding:12px 24px;" +
               "border-radius:6px;text-decoration:none;font-weight:bold;";
    }

    private String formatEur(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.FRANCE).format(amount);
    }

    private String statusLabel(OrderStatus status) {
        return switch (status) {
            case PAYMENT_PENDING -> "Paiement en attente";
            case PAID            -> "Payée";
            case PENDING         -> "En attente";
            case CONFIRMED       -> "Confirmée";
            case PREPARING       -> "En préparation";
            case SHIPPED         -> "Expédiée";
            case DELIVERED       -> "Livrée";
            case CANCELLED       -> "Annulée";
        };
    }
}
