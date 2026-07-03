package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.SendOtpRequest;
import com.thalicloud.auth.dto.request.VerifyOtpRequest;
import com.thalicloud.auth.dto.response.CustomerAuthResponse;
import com.thalicloud.auth.service.CustomerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    /**
     * POST /api/auth/send-otp
     * Phase 1: records the OTP entry (hardcoded "1234") — no SMS sent.
     * Phase 2: integrate SMS gateway here.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        customerAuthService.sendOtp(request.getPhone());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/auth/verify-otp
     * Returns { accessToken, refreshToken, isNewUser }.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<CustomerAuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(customerAuthService.verifyOtp(request.getPhone(), request.getOtp()));
    }

    /**
     * POST /api/auth/customer/refresh
     * Silent token rotation — called by RTK Query re-auth middleware on 401.
     */
    @PostMapping("/customer/refresh")
    public ResponseEntity<CustomerAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(customerAuthService.refreshToken(request.getRefreshToken()));
    }

    /**
     * POST /api/auth/customer/logout
     * Revokes the refresh token; client discards the access token locally.
     */
    @PostMapping("/customer/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        customerAuthService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}
