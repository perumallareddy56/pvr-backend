package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "serviceable_pincodes", indexes = {
    @Index(name = "idx_pincode", columnList = "pincode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceablePincode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pincode;

    @Builder.Default
    @Column(nullable = false)
    private boolean serviceable = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer estimatedDays = 3;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private boolean codAvailable = true;
}
