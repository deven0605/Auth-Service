package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.DeliveryPartnerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryPartnerRefreshTokenRepository extends JpaRepository<DeliveryPartnerRefreshToken, UUID> {
    Optional<DeliveryPartnerRefreshToken> findByToken(String token);
}
