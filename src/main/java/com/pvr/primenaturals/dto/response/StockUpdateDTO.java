package com.pvr.primenaturals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateDTO {
    private Long productId;
    private Integer newStockQuantity;
}
