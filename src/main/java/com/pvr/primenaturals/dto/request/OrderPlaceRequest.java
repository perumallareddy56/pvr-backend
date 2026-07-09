package com.pvr.primenaturals.dto.request;

import lombok.Data;

@Data
public class OrderPlaceRequest {
    private Long addressId;
    private String paymentMethod; // COD, ONLINE
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String orderNotes;
    private String deliveryInstructions;
    private String couponCode;
}
