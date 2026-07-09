package com.pvr.primenaturals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g. "BLOCK_USER", "DELETE_REVIEW", "UPDATE_PRICE"

    @Column(nullable = false)
    private String operatorEmail; // email of admin/manager who did it

    @Column(columnDefinition = "TEXT")
    private String details; // JSON or text description of changes

    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
