package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipping_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String country; // e.g. "IN", "US"

    @Column(nullable = false)
    private BigDecimal minOrder;

    @Column(nullable = false)
    private Double maxWeight; // in kg

    @Column(nullable = false)
    private BigDecimal shippingCharge;

    @Column(nullable = false)
    private BigDecimal freeShippingAbove;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
