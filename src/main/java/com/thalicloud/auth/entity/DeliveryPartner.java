package com.thalicloud.auth.entity;

import com.thalicloud.auth.enums.PartnerLifecycleState;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PartnerLifecycleState lifecycleState = PartnerLifecycleState.PENDING_VERIFICATION;

    // false until the M2 registration form is submitted — mirrors Customer.profileComplete.
    // Drives the isNewUser flag so the app knows to route to the Registration flow.
    @Builder.Default
    @Column(nullable = false)
    private boolean registrationComplete = false;

    // Duty/online status — set by later milestones (M4). Read here only to
    // enforce FR-1.13: logout is blocked while the partner is ONLINE.
    @Builder.Default
    @Column(nullable = false)
    private boolean onDuty = false;

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
