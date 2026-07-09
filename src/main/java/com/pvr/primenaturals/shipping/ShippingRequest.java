package com.pvr.primenaturals.shipping;

import lombok.Data;
import java.util.List;

@Data
public class ShippingRequest {
    private List<Long> productIds;
    private String pincode;
    private String country;
}
