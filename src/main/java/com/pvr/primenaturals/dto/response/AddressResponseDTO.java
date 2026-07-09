package com.pvr.primenaturals.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDTO {
    private Long id;
    private String receiverName;
    private String streetAddress;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String phoneNumber;
    private String addressLabel;
    private boolean isDefault;
}
