package com.pvr.primenaturals.dto.request;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private Long orderId;
    private String reason;
    private String description;
}
