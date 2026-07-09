package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String paymentId;
    private String paymentMethod;
    private String paymentStatus = "PENDING"; // PENDING, COMPLETED, FAILED, REFUNDED

    // Shipping Engine Snapshot Fields
    private BigDecimal shippingCharge;
    private java.time.LocalDateTime deliveryDate;
    private String courierName;
    private String trackingNumber;
    private String warehouseName;
    private String country = "IN";
    private String currency = "INR";

    // Decoupled Shipping Details
    private String receiverName;
    private String streetAddress;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String phoneNumber;

    // Delivery and Special Notes
    private String orderNotes;
    private String deliveryInstructions;

    // Extra Pricing Info
    private BigDecimal deliveryCharge = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String couponCode;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
