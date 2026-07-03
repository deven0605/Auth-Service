package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.CustomerOtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerOtpEntryRepository extends JpaRepository<CustomerOtpEntry, UUID> {
    Optional<CustomerOtpEntry> findByPhone(String phone);
}
