package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String weight; // e.g. "100g", "250g", "500g", "1kg"

    @Column(nullable = false)
    private BigDecimal price; // selling price

    @Column(nullable = false)
    private BigDecimal mrp; // original MRP

    @Column(nullable = false)
    private Integer stockQuantity;

    private String sku;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
