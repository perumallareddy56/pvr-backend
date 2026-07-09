package com.pvr.primenaturals.dto.request;

import lombok.Data;

@Data
public class CartRequest {
    private Long productId;
    private Long variantId;
    private Integer quantity;
}
