package com.thalicloud.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "kitchens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kitchen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false, unique = true)
    private Vendor vendor;

    @Column(name = "kitchen_name", nullable = false, length = 150)
    private String kitchenName;

    @Column(name = "contact_number", nullable = false, length = 10)
    private String contactNumber;

    @Column(name = "opens_at")
    private LocalTime opensAt;

    @Column(name = "closes_at")
    private LocalTime closesAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Nullable (not NOT NULL) even though these are conceptually required — Hibernate's
    // ddl-auto: update emits a plain ADD COLUMN with no DEFAULT, which H2 rejects as NOT
    // NULL against a table that may already have rows. Callers null-coalesce to sensible
    // defaults (see KitchenDiscoveryServiceImpl) instead.
    @Builder.Default
    @Column(name = "is_veg")
    private Boolean veg = true;

    @Builder.Default
    @Column(name = "eta_min_minutes")
    private Integer etaMinMinutes = 25;

    @Builder.Default
    @Column(name = "eta_max_minutes")
    private Integer etaMaxMinutes = 40;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "kitchen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private KitchenAddress address;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
