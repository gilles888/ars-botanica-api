package com.bloomstudio.api.service;

import com.bloomstudio.api.dto.request.UpdateUserRequest;
import com.bloomstudio.api.dto.response.UserResponse;
import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.exception.ResourceNotFoundException;
import com.bloomstudio.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findById(id));
    }

    public UserResponse getMe(String email) {
        return UserResponse.from(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé")));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, String email) {
        User user = findById(id);
        // Only the user themselves or an ADMIN can update
        if (!user.getEmail().equals(email) && !isAdmin(email)) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé");
        }
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());
        if (request.getPhone()     != null) user.setPhone(request.getPhone());
        if (request.getPassword()  != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        // Mise à jour de l'adresse de livraison par défaut
        if (request.getAddress()   != null) user.setAddress(request.getAddress());
        if (request.getCity()      != null) user.setCity(request.getCity());
        if (request.getZip()       != null) user.setZip(request.getZip());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(findById(id));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + id));
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getRole().name().equals("ADMIN"))
                .orElse(false);
    }
}
