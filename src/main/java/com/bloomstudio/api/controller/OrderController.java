package com.bloomstudio.api.controller;

import com.bloomstudio.api.dto.request.OrderRequest;
import com.bloomstudio.api.dto.response.OrderResponse;
import com.bloomstudio.api.enums.OrderStatus;
import com.bloomstudio.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Commandes")
@SecurityRequirement(name = "Bearer Auth")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/me")
    @Operation(summary = "Mes commandes")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.getMyOrders(user.getUsername()));
    }

    @GetMapping("/me/{id}")
    @Operation(summary = "Détail d'une de mes commandes")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.getMyOrder(id, user.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Passer une commande")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request,
                                                 @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request, user.getUsername()));
    }

    @PatchMapping("/me/{id}/cancel")
    @Operation(summary = "Annuler ma commande")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.cancel(id, user.getUsername()));
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toutes les commandes (ADMIN)")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Commande par ID (ADMIN)")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Changer le statut (ADMIN)")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
