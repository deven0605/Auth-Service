package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.SendOtpRequest;
import com.thalicloud.auth.dto.request.VerifyOtpRequest;
import com.thalicloud.auth.dto.response.PartnerAuthResponse;
import com.thalicloud.auth.service.DeliveryPartnerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/partner")
@RequiredArgsConstructor
public class DeliveryPartnerAuthController {

    private final DeliveryPartnerAuthService deliveryPartnerAuthService;

    /**
     * POST /api/auth/partner/send-otp
     * Phase 1: records the OTP entry (hardcoded "1234") — no SMS sent (FR-1.4/FR-1.5).
     */
    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        deliveryPartnerAuthService.sendOtp(request.getPhone());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/auth/partner/verify-otp
     * Returns { accessToken, refreshToken, isNewUser, lifecycleState } (FR-1.8).
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<PartnerAuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(deliveryPartnerAuthService.verifyOtp(request.getPhone(), request.getOtp()));
    }

    /**
     * POST /api/auth/partner/refresh
     * Silent token rotation — called on 401 (FR-1.12).
     */
    @PostMapping("/refresh")
    public ResponseEntity<PartnerAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(deliveryPartnerAuthService.refreshToken(request.getRefreshToken()));
    }

    /**
     * POST /api/auth/partner/logout
     * Revokes the refresh token. Rejected while the partner is ONLINE (FR-1.13).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        deliveryPartnerAuthService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}
