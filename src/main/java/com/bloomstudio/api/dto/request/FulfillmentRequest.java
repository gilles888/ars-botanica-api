package com.bloomstudio.api.dto.request;

import com.bloomstudio.api.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FulfillmentRequest {
    @NotNull
    private OrderStatus status;
    private String trackingNumber;
    private String carrier;
    private String fulfillmentNote;
}
