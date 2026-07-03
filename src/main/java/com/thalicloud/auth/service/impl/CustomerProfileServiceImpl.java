package com.thalicloud.auth.service.impl;

import com.thalicloud.auth.dto.request.CreateCustomerProfileRequest;
import com.thalicloud.auth.dto.request.UpdateCustomerProfileRequest;
import com.thalicloud.auth.dto.response.CustomerProfileResponse;
import com.thalicloud.auth.entity.Customer;
import com.thalicloud.auth.exception.DuplicateResourceException;
import com.thalicloud.auth.exception.ResourceNotFoundException;
import com.thalicloud.auth.repository.CustomerRepository;
import com.thalicloud.auth.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(String phone) {
        Customer customer = findByPhone(phone);
        return CustomerProfileResponse.from(customer);
    }

    @Override
    @Transactional
    public CustomerProfileResponse createProfile(String phone, CreateCustomerProfileRequest request) {
        Customer customer = findByPhone(phone);

        if (customer.isProfileComplete()) {
            throw new DuplicateResourceException("Profile already exists. Use PUT to update.");
        }

        customer.setName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setProfilePicUrl(request.getProfilePicUrl());
        customer.setProfileComplete(true);

        return CustomerProfileResponse.from(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(String phone, UpdateCustomerProfileRequest request) {
        Customer customer = findByPhone(phone);

        if (request.getFullName() != null)    customer.setName(request.getFullName());
        if (request.getEmail() != null)       customer.setEmail(request.getEmail());
        if (request.getProfilePicUrl() != null) customer.setProfilePicUrl(request.getProfilePicUrl());

        return CustomerProfileResponse.from(customerRepository.save(customer));
    }

    private Customer findByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + phone));
    }
}
