package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.SendOtpRequest;
import com.thalicloud.auth.dto.request.VerifyOtpRequest;
import com.thalicloud.auth.dto.response.CustomerAuthResponse;
import com.thalicloud.auth.service.CustomerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Customer Auth", description = "OTP-based login, token refresh, and logout for customer accounts.")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    /**
     * POST /api/auth/send-otp
     * Phase 1: records the OTP entry (hardcoded "1234") — no SMS sent.
     * Phase 2: integrate SMS gateway here.
     */
    @Operation(summary = "Send OTP to a customer phone number",
            description = "Records an OTP entry for the given phone number to start customer login/registration. Phase 1: no SMS is actually sent, the OTP is hardcoded to \"1234\".")
    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("sendOtp: start, phone={}", request.getPhone());
        try {
            customerAuthService.sendOtp(request.getPhone());
            ResponseEntity<Void> response = ResponseEntity.ok().build();
            log.info("sendOtp: end, phone={}", request.getPhone());
            return response;
        } catch (Exception e) {
            log.error("sendOtp: failed, phone={}", request.getPhone(), e);
            throw e;
        }
    }

    /**
     * POST /api/auth/verify-otp
     * Returns { accessToken, refreshToken, isNewUser }.
     */
    @Operation(summary = "Verify customer OTP and issue tokens",
            description = "Validates the OTP entered for the given phone number and, on success, logs in (or creates) the customer, returning an access token, refresh token, and whether this is a newly created user.")
    @PostMapping("/verify-otp")
    public ResponseEntity<CustomerAuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("verifyOtp: start, phone={}", request.getPhone());
        try {
            ResponseEntity<CustomerAuthResponse> response =
                    ResponseEntity.ok(customerAuthService.verifyOtp(request.getPhone(), request.getOtp()));
            log.info("verifyOtp: end, phone={}", request.getPhone());
            return response;
        } catch (Exception e) {
            log.error("verifyOtp: failed, phone={}", request.getPhone(), e);
            throw e;
        }
    }

    /**
     * POST /api/auth/customer/refresh
     * Silent token rotation — called by RTK Query re-auth middleware on 401.
     */
    @Operation(summary = "Refresh a customer access token",
            description = "Exchanges a valid customer refresh token for a new access/refresh token pair. Used for silent token rotation when an API call returns 401.")
    @PostMapping("/customer/refresh")
    public ResponseEntity<CustomerAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("refresh: start");
        try {
            ResponseEntity<CustomerAuthResponse> response =
                    ResponseEntity.ok(customerAuthService.refreshToken(request.getRefreshToken()));
            log.info("refresh: end");
            return response;
        } catch (Exception e) {
            log.error("refresh: failed", e);
            throw e;
        }
    }

    /**
     * POST /api/auth/customer/logout
     * Revokes the refresh token; client discards the access token locally.
     */
    @Operation(summary = "Log out a customer",
            description = "Revokes the supplied customer refresh token server-side; the client is expected to discard the access token locally.")
    @PostMapping("/customer/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        log.info("logout: start");
        try {
            customerAuthService.logout(request.getRefreshToken());
            ResponseEntity<Void> response = ResponseEntity.ok().build();
            log.info("logout: end");
            return response;
        } catch (Exception e) {
            log.error("logout: failed", e);
            throw e;
        }
    }
}
