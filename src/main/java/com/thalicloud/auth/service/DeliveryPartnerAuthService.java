package com.thalicloud.auth.service;

import com.thalicloud.auth.dto.response.PartnerAuthResponse;

public interface DeliveryPartnerAuthService {
    void sendOtp(String phone);
    PartnerAuthResponse verifyOtp(String phone, String otp);
    PartnerAuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
