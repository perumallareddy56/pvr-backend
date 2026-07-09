package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer damagedQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 5;

    private String batchNumber;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;
}
