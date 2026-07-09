package com.pvr.primenaturals.shipping;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ShippingResponse {
    private boolean serviceable;
    private String deliveryDate; // e.g. "2026-07-12"
    private BigDecimal shippingCharge;
    private boolean codAvailable;
}
