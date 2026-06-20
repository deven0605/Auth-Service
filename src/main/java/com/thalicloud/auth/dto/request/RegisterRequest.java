package com.thalicloud.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    // ── Basic Info ────────────────────────────────────────────────────────────

    @NotBlank(message = "Kitchen name is required")
    @Size(max = 150, message = "Kitchen name must not exceed 150 characters")
    private String kitchenName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Owner mobile is required")
    @Pattern(regexp = "^\\d{10}$", message = "Enter a valid 10-digit mobile number")
    private String ownerMobile;

    @NotBlank(message = "Kitchen contact number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Enter a valid 10-digit contact number")
    private String contactNumber;

    // ── Location ──────────────────────────────────────────────────────────────

    private String streetAddress;
    private String city;
    private String state;

    @Pattern(regexp = "^(\\d{6})?$", message = "Enter a valid 6-digit pincode")
    private String pincode;

    private String latitude;
    private String longitude;

    // ── Operating Hours ───────────────────────────────────────────────────────

    @NotBlank(message = "Opening time is required")
    private String opensAt;

    @NotBlank(message = "Closing time is required")
    private String closesAt;
}
