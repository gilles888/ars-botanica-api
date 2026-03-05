package com.bloomstudio.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotEmpty(message = "La commande doit contenir au moins un article")
    private List<OrderItemRequest> items;

    @NotBlank(message = "L'adresse est obligatoire")
    private String deliveryAddress;

    @NotBlank(message = "Le code postal est obligatoire")
    private String deliveryZip;

    @NotBlank(message = "La ville est obligatoire")
    private String deliveryCity;

    private String deliveryMethod = "standard";
    private String deliveryNotes;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        @Min(value = 1, message = "La quantité doit être au moins 1")
        private Integer quantity;
    }
}
