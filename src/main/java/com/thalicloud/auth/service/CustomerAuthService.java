package com.thalicloud.auth.service;

import com.thalicloud.auth.dto.response.CustomerAuthResponse;

public interface CustomerAuthService {
    void sendOtp(String phone);
    CustomerAuthResponse verifyOtp(String phone, String otp);
    CustomerAuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
