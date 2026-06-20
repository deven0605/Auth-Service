package com.thalicloud.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class VendorProfileResponse {

    // Vendor
    private final UUID vendorId;
    private final String ownerName;
    private final String email;
    private final String ownerMobile;
    private final String role;
    private final boolean active;
    private final boolean verified;
    private final LocalDateTime createdAt;

    // Kitchen
    private final UUID kitchenId;
    private final String kitchenName;
    private final String contactNumber;
    private final LocalTime opensAt;
    private final LocalTime closesAt;

    // Address
    private final String streetAddress;
    private final String city;
    private final String state;
    private final String pincode;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
}
