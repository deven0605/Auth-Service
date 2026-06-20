package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.response.ApiResponse;
import com.thalicloud.auth.dto.response.VendorProfileResponse;
import com.thalicloud.auth.entity.Vendor;
import com.thalicloud.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final AuthService authService;

    /**
     * GET /api/vendors/me  — returns the profile of the currently authenticated vendor.
     * Requires a valid Bearer access token in the Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Vendor vendor) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved", authService.getProfile(vendor.getId()))
        );
    }
}
