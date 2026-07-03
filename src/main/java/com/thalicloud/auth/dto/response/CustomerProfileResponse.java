package com.thalicloud.auth.dto.response;

import com.thalicloud.auth.entity.Customer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerProfileResponse {

    private final String id;
    private final String mobileNumber;
    private final String fullName;
    private final String email;
    private final String profilePicUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CustomerProfileResponse from(Customer c) {
        return CustomerProfileResponse.builder()
                .id(c.getId().toString())
                .mobileNumber(c.getPhone())
                .fullName(c.getName())
                .email(c.getEmail())
                .profilePicUrl(c.getProfilePicUrl())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
