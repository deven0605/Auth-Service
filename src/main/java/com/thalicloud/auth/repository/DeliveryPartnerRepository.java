package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {
    Optional<DeliveryPartner> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
