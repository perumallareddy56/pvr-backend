package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private String weight;

    @ElementCollection
    @CollectionTable(name = "product_process", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "step")
    private List<String> process;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private ProductSubCategory subCategory;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(unique = true)
    private String sku;

    private String barcode;

    @Column(nullable = false)
    private boolean active = true;

    private BigDecimal mrp; // original MRP fallback

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String nutritionInfo;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String howToUse;

    @Column(columnDefinition = "TEXT")
    private String storageInstructions;

    private String shelfLife;

    private String manufacturerDetails;

    private String countryOfOrigin;

    @ElementCollection
    @CollectionTable(name = "product_certifications", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "certification")
    private List<String> certifications = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductVariant> variants = new java.util.ArrayList<>();
}
