package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String email;

    @Column(nullable = false)
    private String type; // e.g. "BOUTIQUE", "HQ", "SUPPORT", "WAREHOUSE"

    @Builder.Default
    private boolean active = true;
}
