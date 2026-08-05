package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.SendOtpRequest;
import com.thalicloud.auth.dto.request.VerifyOtpRequest;
import com.thalicloud.auth.dto.response.PartnerAuthResponse;
import com.thalicloud.auth.service.DeliveryPartnerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/partner")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner Auth", description = "OTP-based login, token refresh, and logout for delivery partner accounts.")
public class DeliveryPartnerAuthController {

    private final DeliveryPartnerAuthService deliveryPartnerAuthService;

    /**
     * POST /api/auth/partner/send-otp
     * Phase 1: records the OTP entry (hardcoded "1234") — no SMS sent (FR-1.4/FR-1.5).
     */
    @Operation(summary = "Send OTP to a delivery partner phone number",
            description = "Records an OTP entry for the given phone number to start delivery partner login/registration (FR-1.4/FR-1.5). Phase 1: no SMS is actually sent, the OTP is hardcoded to \"1234\".")
    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("sendOtp: start, phone={}", request.getPhone());
        try {
            deliveryPartnerAuthService.sendOtp(request.getPhone());
            ResponseEntity<Void> response = ResponseEntity.ok().build();
            log.info("sendOtp: end, phone={}", request.getPhone());
            return response;
        } catch (Exception e) {
            log.error("sendOtp: failed, phone={}", request.getPhone(), e);
            throw e;
        }
    }

    /**
     * POST /api/auth/partner/verify-otp
     * Returns { accessToken, refreshToken, isNewUser, lifecycleState } (FR-1.8).
     */
    @Operation(summary = "Verify delivery partner OTP and issue tokens",
            description = "Validates the OTP entered for the given phone number and, on success, logs in (or creates) the delivery partner, returning an access token, refresh token, whether this is a new user, and the partner's lifecycle state (FR-1.8).")
    @PostMapping("/verify-otp")
    public ResponseEntity<PartnerAuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("verifyOtp: start, phone={}", request.getPhone());
        try {
            ResponseEntity<PartnerAuthResponse> response =
                    ResponseEntity.ok(deliveryPartnerAuthService.verifyOtp(request.getPhone(), request.getOtp()));
            log.info("verifyOtp: end, phone={}", request.getPhone());
            return response;
        } catch (Exception e) {
            log.error("verifyOtp: failed, phone={}", request.getPhone(), e);
            throw e;
        }
    }

    /**
     * POST /api/auth/partner/refresh
     * Silent token rotation — called on 401 (FR-1.12).
     */
    @Operation(summary = "Refresh a delivery partner access token",
            description = "Exchanges a valid delivery partner refresh token for a new access/refresh token pair. Used for silent token rotation when an API call returns 401 (FR-1.12).")
    @PostMapping("/refresh")
    public ResponseEntity<PartnerAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("refresh: start");
        try {
            ResponseEntity<PartnerAuthResponse> response =
                    ResponseEntity.ok(deliveryPartnerAuthService.refreshToken(request.getRefreshToken()));
            log.info("refresh: end");
            return response;
        } catch (Exception e) {
            log.error("refresh: failed", e);
            throw e;
        }
    }

    /**
     * POST /api/auth/partner/logout
     * Revokes the refresh token. Rejected while the partner is ONLINE (FR-1.13).
     */
    @Operation(summary = "Log out a delivery partner",
            description = "Revokes the supplied delivery partner refresh token. The request is rejected while the partner's status is ONLINE (FR-1.13).")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("logout: start");
        try {
            deliveryPartnerAuthService.logout(request.getRefreshToken());
            ResponseEntity<Void> response = ResponseEntity.ok().build();
            log.info("logout: end");
            return response;
        } catch (Exception e) {
            log.error("logout: failed", e);
            throw e;
        }
    }
}
