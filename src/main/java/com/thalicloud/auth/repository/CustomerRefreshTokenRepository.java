package com.thalicloud.auth.repository;

import com.thalicloud.auth.entity.CustomerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRefreshTokenRepository extends JpaRepository<CustomerRefreshToken, UUID> {
    Optional<CustomerRefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE CustomerRefreshToken t SET t.revoked = true WHERE t.customer.id = :customerId")
    void revokeAllByCustomerId(@Param("customerId") UUID customerId);
}
