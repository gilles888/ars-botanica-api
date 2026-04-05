package com.bloomstudio.api.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String password;   // optionnel — null = pas de changement
    // Adresse de livraison par défaut (tous les champs sont optionnels)
    private String address;
    private String city;
    private String zip;
}
