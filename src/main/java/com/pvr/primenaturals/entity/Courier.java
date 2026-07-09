package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "couriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "Delhivery", "Blue Dart", "Mock Courier"

    @Column(nullable = false, unique = true)
    private String code; // e.g. "DELHIVERY", "BLUEDART", "MOCK"

    private String trackingUrlTemplate; // e.g. "https://delhivery.com/track?id={trackingId}"

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
