package com.thalicloud.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResponse {
    private final UUID vendorId;
    private final String email;
    private final String ownerName;
    private final String role;
    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresIn; // seconds
}
