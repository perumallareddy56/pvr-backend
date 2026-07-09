package com.pvr.primenaturals.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subTotal;
    
    // Variant fields
    private Long variantId;
    private String variantWeight;
}
