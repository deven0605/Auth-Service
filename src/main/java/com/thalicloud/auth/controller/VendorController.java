package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.response.ApiResponse;
import com.thalicloud.auth.dto.response.VendorProfileResponse;
import com.thalicloud.auth.entity.Vendor;
import com.thalicloud.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Profile", description = "Read the profile of the currently authenticated vendor.")
public class VendorController {

    private final AuthService authService;

    /**
     * GET /api/vendors/me  — returns the profile of the currently authenticated vendor.
     * Requires a valid Bearer access token in the Authorization header.
     */
    @Operation(summary = "Get the authenticated vendor's profile",
            description = "Returns the profile of the vendor identified by the Bearer access token in the Authorization header.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Vendor vendor) {
        log.info("getMyProfile: start, vendorId={}", vendor.getId());
        try {
            ResponseEntity<ApiResponse<VendorProfileResponse>> response = ResponseEntity.ok(
                    ApiResponse.success("Profile retrieved", authService.getProfile(vendor.getId()))
            );
            log.info("getMyProfile: end, vendorId={}", vendor.getId());
            return response;
        } catch (Exception e) {
            log.error("getMyProfile: failed, vendorId={}", vendor.getId(), e);
            throw e;
        }
    }
}
