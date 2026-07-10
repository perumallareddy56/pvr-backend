package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String platform; // e.g. "INSTAGRAM", "FACEBOOK", "LINKEDIN", "WHATSAPP", "YOUTUBE", "TWITTER"

    @Column(nullable = false)
    private String url;

    @Builder.Default
    private boolean active = true;
}
