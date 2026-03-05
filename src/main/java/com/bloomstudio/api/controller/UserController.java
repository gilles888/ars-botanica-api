package com.bloomstudio.api.controller;

import com.bloomstudio.api.dto.request.UpdateUserRequest;
import com.bloomstudio.api.dto.response.UserResponse;
import com.bloomstudio.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs")
@SecurityRequirement(name = "Bearer Auth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mon profil")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(userService.getMe(user.getUsername()));
    }

    @PutMapping("/me")
    @Operation(summary = "Modifier mon profil")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UpdateUserRequest request,
                                                  @AuthenticationPrincipal UserDetails user) {
        UserResponse me = userService.getMe(user.getUsername());
        return ResponseEntity.ok(userService.update(me.getId(), request, user.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tous les utilisateurs (ADMIN)")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Utilisateur par ID (ADMIN)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un utilisateur (ADMIN)")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                                @RequestBody UpdateUserRequest request,
                                                @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(userService.update(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur (ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
