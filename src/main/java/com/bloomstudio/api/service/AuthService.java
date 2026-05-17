package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.ConvertGuestRequest;
import com.bloomstudio.api.dto.request.ForgotPasswordRequest;
import com.bloomstudio.api.dto.request.LoginRequest;
import com.bloomstudio.api.dto.request.RegisterRequest;
import com.bloomstudio.api.dto.request.ResetPasswordRequest;
import com.bloomstudio.api.dto.response.AuthResponse;
import com.bloomstudio.api.entity.PasswordResetToken;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.Role;
import com.bloomstudio.api.exception.BadRequestException;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.PasswordResetTokenRepository;
import com.bloomstudio.api.repository.UserRepository;
import com.bloomstudio.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CLIENT)
                .build();

        userRepository.save(user);
        emailService.sendWelcomeEmail(user);

        String token = jwtTokenProvider.generateToken(user);
        return buildResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtTokenProvider.generateToken(user);
        return buildResponse(token, user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            resetTokenRepository.deleteByUserId(user.getId());
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            resetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user, resetToken.getToken());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Lien invalide ou expiré"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Ce lien a déjà été utilisé");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Ce lien a expiré");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    @Transactional
    public AuthResponse convertGuest(ConvertGuestRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Aucun compte invité trouvé pour cet email"));

        if (user.getRole() != Role.GUEST) {
            throw new BadRequestException("Un compte existe déjà pour cet email");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user);

        String token = jwtTokenProvider.generateToken(user);
        return buildResponse(token, user);
    }

    private AuthResponse buildResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
