package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_trackings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String status; // PLACED, PACKED, SHIPPED, DELIVERED, CANCELLED

    private String description; // Status details/remarks

    private String location; // Current location or city

    @CreationTimestamp
    private LocalDateTime updatedAt;
}
