package com.thalicloud.auth.controller;

import com.thalicloud.auth.dto.request.CreateCustomerProfileRequest;
import com.thalicloud.auth.dto.request.UpdateCustomerProfileRequest;
import com.thalicloud.auth.dto.response.CustomerProfileResponse;
import com.thalicloud.auth.entity.Customer;
import com.thalicloud.auth.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> getProfile(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(customerProfileService.getProfile(customer.getPhone()));
    }

    @PostMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> createProfile(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody CreateCustomerProfileRequest request) {
        return ResponseEntity.ok(customerProfileService.createProfile(customer.getPhone(), request));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return ResponseEntity.ok(customerProfileService.updateProfile(customer.getPhone(), request));
    }
}
