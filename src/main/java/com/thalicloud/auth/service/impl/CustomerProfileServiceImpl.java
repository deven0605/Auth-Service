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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(String phone) {
        log.info("getProfile: start, phone={}", phone);
        try {
            Customer customer = findByPhone(phone);
            CustomerProfileResponse response = CustomerProfileResponse.from(customer);
            log.info("getProfile: end, phone={}", phone);
            return response;
        } catch (Exception e) {
            log.error("getProfile: failed, phone={}", phone, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CustomerProfileResponse createProfile(String phone, CreateCustomerProfileRequest request) {
        log.info("createProfile: start, phone={}", phone);
        try {
            Customer customer = findByPhone(phone);

            if (customer.isProfileComplete()) {
                throw new DuplicateResourceException("Profile already exists. Use PUT to update.");
            }

            customer.setName(request.getFullName());
            customer.setEmail(request.getEmail());
            customer.setProfilePicUrl(request.getProfilePicUrl());
            customer.setProfileComplete(true);

            CustomerProfileResponse response = CustomerProfileResponse.from(customerRepository.save(customer));
            log.info("createProfile: end, phone={}", phone);
            return response;
        } catch (Exception e) {
            log.error("createProfile: failed, phone={}", phone, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(String phone, UpdateCustomerProfileRequest request) {
        log.info("updateProfile: start, phone={}", phone);
        try {
            Customer customer = findByPhone(phone);

            if (request.getFullName() != null)    customer.setName(request.getFullName());
            if (request.getEmail() != null)       customer.setEmail(request.getEmail());
            if (request.getProfilePicUrl() != null) customer.setProfilePicUrl(request.getProfilePicUrl());

            CustomerProfileResponse response = CustomerProfileResponse.from(customerRepository.save(customer));
            log.info("updateProfile: end, phone={}", phone);
            return response;
        } catch (Exception e) {
            log.error("updateProfile: failed, phone={}", phone, e);
            throw e;
        }
    }

    private Customer findByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + phone));
    }
}
