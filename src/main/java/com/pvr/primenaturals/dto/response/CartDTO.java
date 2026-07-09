package com.pvr.primenaturals.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {
    private Long cartId;
    private List<CartItemDTO> items;
    private BigDecimal total;
}
