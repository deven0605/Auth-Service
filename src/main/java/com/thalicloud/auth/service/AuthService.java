package com.thalicloud.auth.service;

import com.thalicloud.auth.dto.request.LoginRequest;
import com.thalicloud.auth.dto.request.RefreshTokenRequest;
import com.thalicloud.auth.dto.request.RegisterRequest;
import com.thalicloud.auth.dto.response.AuthResponse;
import com.thalicloud.auth.dto.response.VendorProfileResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    VendorProfileResponse getProfile(UUID vendorId);
}
