package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.DeliveryPartnerOtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryPartnerOtpEntryRepository extends JpaRepository<DeliveryPartnerOtpEntry, UUID> {
    Optional<DeliveryPartnerOtpEntry> findByPhone(String phone);
}
