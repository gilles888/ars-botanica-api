package com.bloomstudio.api.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String password;   // optionnel — null = pas de changement
}
