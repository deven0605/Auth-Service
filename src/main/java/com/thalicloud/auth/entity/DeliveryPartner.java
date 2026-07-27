package com.thalicloud.auth.entity;

import com.thalicloud.auth.enums.DutyStatus;
import com.thalicloud.auth.enums.PartnerLifecycleState;
import com.thalicloud.auth.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner implements UserDetails {

    // JWT subject prefix — disambiguates a partner's phone from a Customer's
    // phone (same "+91XXXXXXXXXX" shape) in the shared UserDetailsService.
    public static final String USERNAME_PREFIX = "PARTNER:";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 15)
    private String phone; // "+91XXXXXXXXXX"

    @Column(length = 100)
    private String name; // filled during Registration (M2)

    // ── M2.1 Personal Details — owned/written by delivery-service ──────────────
    private LocalDate dob;

    @Column(length = 20)
    private String gender; // optional (FR-2.1)

    @Column(length = 150)
    private String email; // optional (FR-2.1)

    @Column(length = 2000)
    private String profilePhotoUrl; // MinIO object URL for the required selfie

    // ── M2.2 Vehicle Details — owned/written by delivery-service ───────────────
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VehicleType vehicleType;

    @Column(length = 20)
    private String vehicleNumber; // required if motorized (FR-2.3)

    @Column(length = 100)
    private String vehicleModel;

    // ── M2.4 Bank Details — owned/written by delivery-service ──────────────────
    @Column(length = 100)
    private String bankAccountHolderName;

    @Column(length = 30)
    private String bankAccountNumber;

    @Column(length = 15)
    private String bankIfscCode;

    @Column(length = 100)
    private String upiId;

    // ── M12/FR-12 push notifications — owned/written by delivery-service ───────
    @Column(length = 200)
    private String expoPushToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PartnerLifecycleState lifecycleState = PartnerLifecycleState.PENDING_VERIFICATION;

    // false until the M2 registration form is submitted — mirrors Customer.profileComplete.
    // Drives the isNewUser flag so the app knows to route to the Registration flow.
    @Builder.Default
    @Column(nullable = false)
    private boolean registrationComplete = false;

    // ── M3.1 Availability — owned/written by delivery-service. Read here only
    // to enforce FR-1.13: logout is blocked while the partner is on duty. ──────
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DutyStatus dutyStatus = DutyStatus.OFFLINE;

    private Double currentLatitude;

    private Double currentLongitude;

    private LocalDateTime lastLocationAt;

    // ── M3.2 Dashboard — no rating engine yet; starts at the platform default. ──
    @Builder.Default
    private Double rating = 5.0;

    // ── M4.2 — FR-4.8: raw counters backing a future cancellation-rate metric. ──
    @Column(nullable = false)
    private int totalAssignments;

    @Column(nullable = false)
    private int cancelledAssignments;

    // ── M7 — Cash on Delivery (COD) Collection. Owned/written by
    // delivery-service (FR-7.2 credits it, FR-7.4's remit flow debits it). ──
    @Builder.Default
    @Column(nullable = false)
    private long cashInHandPaise = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public String getUsername() {
        return USERNAME_PREFIX + phone;
    }

    @Override
    public String getPassword() {
        return ""; // OTP-based login — no password
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_DELIVERY_PARTNER"));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
