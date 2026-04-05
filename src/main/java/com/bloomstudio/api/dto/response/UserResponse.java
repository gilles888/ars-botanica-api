package com.bloomstudio.api.dto.response;

import com.bloomstudio.api.entity.User;
import com.bloomstudio.api.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    // Adresse de livraison par défaut
    private String address;
    private String city;
    private String zip;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .address(u.getAddress())
                .city(u.getCity())
                .zip(u.getZip())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
