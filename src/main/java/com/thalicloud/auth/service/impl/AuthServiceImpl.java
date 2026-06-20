package com.thalicloud.auth.service.impl;

import com.thalicloud.auth.dto.request.LoginRequest;
import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.RegisterRequest;
import com.thalicloud.auth.dto.response.AuthResponse;
import com.thalicloud.auth.dto.response.VendorProfileResponse;
import com.thalicloud.auth.entity.Kitchen;
import com.thalicloud.auth.entity.KitchenAddress;
import com.thalicloud.auth.entity.RefreshToken;
import com.thalicloud.auth.entity.Vendor;
import com.thalicloud.auth.enums.VendorRole;
import com.thalicloud.auth.exception.AuthException;
import com.thalicloud.auth.exception.DuplicateResourceException;
import com.thalicloud.auth.exception.ResourceNotFoundException;
import com.thalicloud.auth.mapper.VendorMapper;
import com.thalicloud.auth.repository.KitchenAddressRepository;
import com.thalicloud.auth.repository.KitchenRepository;
import com.thalicloud.auth.repository.RefreshTokenRepository;
import com.thalicloud.auth.repository.VendorRepository;
import com.thalicloud.auth.service.AuthService;
import com.thalicloud.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final VendorRepository vendorRepository;
    private final KitchenRepository kitchenRepository;
    private final KitchenAddressRepository kitchenAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (vendorRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + req.getEmail());
        }

        Vendor vendor = Vendor.builder()
                .ownerName(req.getOwnerName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .ownerMobile(req.getOwnerMobile())
                .role(VendorRole.VENDOR)
                .build();
        vendorRepository.save(vendor);

        Kitchen kitchen = Kitchen.builder()
                .vendor(vendor)
                .kitchenName(req.getKitchenName())
                .contactNumber(req.getContactNumber())
                .opensAt(parseTime(req.getOpensAt()))
                .closesAt(parseTime(req.getClosesAt()))
                .build();
        kitchenRepository.save(kitchen);

        KitchenAddress address = KitchenAddress.builder()
                .kitchen(kitchen)
                .streetAddress(req.getStreetAddress())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .latitude(parseDecimal(req.getLatitude()))
                .longitude(parseDecimal(req.getLongitude()))
                .build();
        kitchenAddressRepository.save(address);

        String accessToken  = jwtService.generateAccessToken(vendor);
        String refreshToken = jwtService.generateRefreshToken(vendor);
        persistRefreshToken(vendor, refreshToken);

        return buildAuthResponse(vendor, accessToken, refreshToken);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        Vendor vendor = vendorRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        refreshTokenRepository.revokeAllByVendorId(vendor.getId());

        String accessToken  = jwtService.generateAccessToken(vendor);
        String refreshToken = jwtService.generateRefreshToken(vendor);
        persistRefreshToken(vendor, refreshToken);

        return buildAuthResponse(vendor, accessToken, refreshToken);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new AuthException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh token has expired");
        }

        Vendor vendor = stored.getVendor();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken  = jwtService.generateAccessToken(vendor);
        String newRefreshToken = jwtService.generateRefreshToken(vendor);
        persistRefreshToken(vendor, newRefreshToken);

        return buildAuthResponse(vendor, newAccessToken, newRefreshToken);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public VendorProfileResponse getProfile(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorId));
        return VendorMapper.toProfileResponse(vendor);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void persistRefreshToken(Vendor vendor, String tokenStr) {
        RefreshToken rt = RefreshToken.builder()
                .vendor(vendor)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(rt);
    }

    private AuthResponse buildAuthResponse(Vendor vendor, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .vendorId(vendor.getId())
                .email(vendor.getEmail())
                .ownerName(vendor.getOwnerName())
                .role(vendor.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(900L)
                .build();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalTime.parse(value);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value); }
        catch (NumberFormatException e) { return null; }
    }
}
