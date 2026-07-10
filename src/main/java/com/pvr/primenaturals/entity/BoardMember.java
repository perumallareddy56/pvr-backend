package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String initials;

    @Column(length = 2000)
    private String bio;

    private String imageUrl;

    private int displayOrder;

    @Builder.Default
    private boolean active = true;
}
