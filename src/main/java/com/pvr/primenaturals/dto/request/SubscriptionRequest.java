package com.pvr.primenaturals.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SubscriptionRequest {
    private Long productId;
    private int quantity;
    private String frequency; // WEEKLY, BIWEEKLY, MONTHLY
    private LocalDate startDate;
    private String variantWeight;
}
