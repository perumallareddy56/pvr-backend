package com.pvr.primenaturals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponseDTO {
    private Long id;
    private String weight;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer stockQuantity;
    private String sku;
    private boolean active;
}
