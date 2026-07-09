package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false)
    private String frequency; // WEEKLY, BIWEEKLY, MONTHLY

    @Column(nullable = false)
    private LocalDate nextDeliveryDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerDelivery;

    private String variantWeight; // optional variant info

    @CreationTimestamp
    private LocalDateTime createdAt;
}
