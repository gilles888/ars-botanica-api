package com.bloomstudio.api.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuestOrderRequest extends OrderRequest {
    private String guestEmail;
    private String guestFirstName;
    private String guestLastName;
    private String guestPhone;
}
