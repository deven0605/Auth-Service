package com.thalicloud.auth.service;

import com.thalicloud.auth.dto.request.CreateCustomerProfileRequest;
import com.thalicloud.auth.dto.request.UpdateCustomerProfileRequest;
import com.thalicloud.auth.dto.response.CustomerProfileResponse;

public interface CustomerProfileService {
    CustomerProfileResponse getProfile(String phone);
    CustomerProfileResponse createProfile(String phone, CreateCustomerProfileRequest request);
    CustomerProfileResponse updateProfile(String phone, UpdateCustomerProfileRequest request);
}
