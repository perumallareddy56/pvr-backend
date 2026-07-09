package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String discountType; // PERCENTAGE or FLAT or FREE_DELIVERY

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount; // Cap for PERCENTAGE type

    private LocalDate expiryDate;

    private Integer usageLimit;     // null = unlimited
    private int timesUsed = 0;

    private boolean active = true;

    // Targeting rules
    private Long specificUserId;      // null = any user; set to target a specific user
    private String targetCategory;   // null = all categories; specific category name to restrict
    private boolean firstOrderOnly = false; // if true, only valid for user's very first order
}

