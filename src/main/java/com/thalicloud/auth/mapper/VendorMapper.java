package com.thalicloud.auth.mapper;

import com.thalicloud.auth.dto.response.VendorProfileResponse;
import com.thalicloud.auth.entity.Kitchen;
import com.thalicloud.auth.entity.KitchenAddress;
import com.thalicloud.auth.entity.Vendor;

public final class VendorMapper {

    private VendorMapper() {}

    public static VendorProfileResponse toProfileResponse(Vendor vendor) {
        VendorProfileResponse.VendorProfileResponseBuilder builder = VendorProfileResponse.builder()
                .vendorId(vendor.getId())
                .ownerName(vendor.getOwnerName())
                .email(vendor.getEmail())
                .ownerMobile(vendor.getOwnerMobile())
                .role(vendor.getRole().name())
                .active(vendor.isActive())
                .verified(vendor.isVerified())
                .createdAt(vendor.getCreatedAt());

        Kitchen kitchen = vendor.getKitchen();
        if (kitchen != null) {
            builder.kitchenId(kitchen.getId())
                   .kitchenName(kitchen.getKitchenName())
                   .contactNumber(kitchen.getContactNumber())
                   .opensAt(kitchen.getOpensAt())
                   .closesAt(kitchen.getClosesAt());

            KitchenAddress address = kitchen.getAddress();
            if (address != null) {
                builder.streetAddress(address.getStreetAddress())
                       .city(address.getCity())
                       .state(address.getState())
                       .pincode(address.getPincode())
                       .latitude(address.getLatitude())
                       .longitude(address.getLongitude());
            }
        }

        return builder.build();
    }
}
