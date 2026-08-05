package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.CreateCustomerProfileRequest;
import com.thalicloud.auth.dto.request.UpdateCustomerProfileRequest;
import com.thalicloud.auth.dto.response.CustomerProfileResponse;
import com.thalicloud.auth.entity.Customer;
import com.thalicloud.auth.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Tag(name = "Customer Profile", description = "Read, create, and update the profile of the currently authenticated customer.")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @Operation(summary = "Get the authenticated customer's profile",
            description = "Returns the profile of the customer identified by the Bearer access token, looked up by phone number.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> getProfile(
            @AuthenticationPrincipal Customer customer) {
        log.info("getProfile: start, phone={}", customer.getPhone());
        try {
            ResponseEntity<CustomerProfileResponse> response =
                    ResponseEntity.ok(customerProfileService.getProfile(customer.getPhone()));
            log.info("getProfile: end, phone={}", customer.getPhone());
            return response;
        } catch (Exception e) {
            log.error("getProfile: failed, phone={}", customer.getPhone(), e);
            throw e;
        }
    }

    @Operation(summary = "Create the authenticated customer's profile",
            description = "Creates the profile (name, address, etc.) for the customer identified by the Bearer access token, using the supplied request body.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> createProfile(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody CreateCustomerProfileRequest request) {
        log.info("createProfile: start, phone={}", customer.getPhone());
        try {
            ResponseEntity<CustomerProfileResponse> response =
                    ResponseEntity.ok(customerProfileService.createProfile(customer.getPhone(), request));
            log.info("createProfile: end, phone={}", customer.getPhone());
            return response;
        } catch (Exception e) {
            log.error("createProfile: failed, phone={}", customer.getPhone(), e);
            throw e;
        }
    }

    @Operation(summary = "Update the authenticated customer's profile",
            description = "Updates the existing profile of the customer identified by the Bearer access token with the supplied fields.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        log.info("updateProfile: start, phone={}", customer.getPhone());
        try {
            ResponseEntity<CustomerProfileResponse> response =
                    ResponseEntity.ok(customerProfileService.updateProfile(customer.getPhone(), request));
            log.info("updateProfile: end, phone={}", customer.getPhone());
            return response;
        } catch (Exception e) {
            log.error("updateProfile: failed, phone={}", customer.getPhone(), e);
            throw e;
        }
    }
}
