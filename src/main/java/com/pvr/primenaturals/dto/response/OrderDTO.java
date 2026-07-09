package com.pvr.primenaturals.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentId;
    private String paymentMethod;
    private String paymentStatus;

    // Shipping details
    private String receiverName;
    private String streetAddress;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String phoneNumber;

    // Notes
    private String orderNotes;
    private String deliveryInstructions;

    // Pricing details
    private BigDecimal deliveryCharge;
    private BigDecimal discountAmount;
    private String couponCode;

    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    // Shipping Engine Snapshot Fields
    private BigDecimal shippingCharge;
    private String deliveryDate; // Formatted date string
    private String courierName;
    private String trackingNumber;
    private String warehouseName;
    private String country;
    private String currency;
}
