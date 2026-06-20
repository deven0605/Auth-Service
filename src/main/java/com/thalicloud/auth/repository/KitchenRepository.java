package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.Kitchen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KitchenRepository extends JpaRepository<Kitchen, UUID> {
    Optional<Kitchen> findByVendorId(UUID vendorId);
}
