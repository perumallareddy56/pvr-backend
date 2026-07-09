package com.pvr.primenaturals.dto.request;

import lombok.Data;

@Data
public class AddressRequest {
    private String receiverName;
    private String streetAddress;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String phoneNumber;
    private String addressLabel; // HOME, WORK, OTHER
    private boolean isDefault;
}
