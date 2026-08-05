package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.LoginRequest;
import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.RegisterRequest;
import com.thalicloud.auth.dto.response.ApiResponse;
import com.thalicloud.auth.dto.response.AuthResponse;
import com.thalicloud.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Vendor Auth", description = "Registration, login, token refresh, and logout for vendor accounts.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new vendor",
            description = "Creates a new vendor account from the supplied registration details and returns an access/refresh token pair for the newly created account.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("register: start, email={}", request.getEmail());
        try {
            ResponseEntity<ApiResponse<AuthResponse>> response = ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Vendor registered successfully", authService.register(request)));
            log.info("register: end, email={}", request.getEmail());
            return response;
        } catch (Exception e) {
            log.error("register: failed, email={}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Log in a vendor",
            description = "Authenticates a vendor using email and password credentials and returns a new access/refresh token pair on success.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("login: start, email={}", request.getEmail());
        try {
            ResponseEntity<ApiResponse<AuthResponse>> response =
                    ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
            log.info("login: end, email={}", request.getEmail());
            return response;
        } catch (Exception e) {
            log.error("login: failed, email={}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Refresh a vendor access token",
            description = "Exchanges a valid, unexpired refresh token for a new access/refresh token pair, without requiring the vendor to log in again.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("refresh: start");
        try {
            ResponseEntity<ApiResponse<AuthResponse>> response =
                    ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request)));
            log.info("refresh: end");
            return response;
        } catch (Exception e) {
            log.error("refresh: failed", e);
            throw e;
        }
    }

    @Operation(summary = "Log out a vendor",
            description = "Revokes the supplied refresh token so it can no longer be used to obtain new access tokens.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest request) {
        log.info("logout: start");
        try {
            authService.logout(request.getRefreshToken());
            ResponseEntity<ApiResponse<Void>> response = ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
            log.info("logout: end");
            return response;
        } catch (Exception e) {
            log.error("logout: failed", e);
            throw e;
        }
    }
}
