package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_sub_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;
}
