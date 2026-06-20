package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.KitchenAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KitchenAddressRepository extends JpaRepository<KitchenAddress, UUID> {
    Optional<KitchenAddress> findByKitchenId(UUID kitchenId);
}
