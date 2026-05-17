package com.bloomstudio.api.controller;

import com.bloomstudio.api.dto.request.ConvertGuestRequest;
import com.bloomstudio.api.dto.request.ForgotPasswordRequest;
import com.bloomstudio.api.dto.request.LoginRequest;
import com.bloomstudio.api.dto.request.RegisterRequest;
import com.bloomstudio.api.dto.request.ResetPasswordRequest;
import com.bloomstudio.api.dto.response.AuthResponse;
import com.bloomstudio.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Créer un compte")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demander une réinitialisation de mot de passe")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe avec le token reçu par email")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/convert-guest")
    @Operation(summary = "Convertir un compte invité en compte utilisateur")
    public ResponseEntity<AuthResponse> convertGuest(@Valid @RequestBody ConvertGuestRequest request) {
        return ResponseEntity.ok(authService.convertGuest(request));
    }
}
