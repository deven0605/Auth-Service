package com.thalicloud.auth.service.impl;

import com.thalicloud.auth.dto.response.PartnerAuthResponse;
import com.thalicloud.auth.entity.DeliveryPartner;
import com.thalicloud.auth.entity.DeliveryPartnerOtpEntry;
import com.thalicloud.auth.entity.DeliveryPartnerRefreshToken;
import com.thalicloud.auth.enums.DutyStatus;
import com.thalicloud.auth.exception.AuthException;
import com.thalicloud.auth.repository.DeliveryPartnerOtpEntryRepository;
import com.thalicloud.auth.repository.DeliveryPartnerRefreshTokenRepository;
import com.thalicloud.auth.repository.DeliveryPartnerRepository;
import com.thalicloud.auth.service.DeliveryPartnerAuthService;
import com.thalicloud.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryPartnerAuthServiceImpl implements DeliveryPartnerAuthService {

    // Phase 1: SMS gateway bypassed — hardcoded OTP (FR-1.5)
    private static final String HARDCODED_OTP     = "1234";
    private static final int    MAX_ATTEMPTS       = 3;    // FR-1.9
    private static final int    LOCKOUT_MINUTES    = 10;   // FR-1.9
    private static final int    OTP_EXPIRY_MINUTES = 5;    // FR-1.7

    private final DeliveryPartnerRepository             partnerRepository;
    private final DeliveryPartnerOtpEntryRepository     otpRepository;
    private final DeliveryPartnerRefreshTokenRepository refreshTokenRepository;
    private final JwtService                            jwtService;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // ── Send OTP (FR-1.4 / FR-1.5) ───────────────────────────────────────────

    @Override
    @Transactional
    public void sendOtp(String phone) {
        log.info("sendOtp: start, phone={}", phone);
        try {
            DeliveryPartnerOtpEntry entry = otpRepository.findByPhone(phone)
                    .orElse(DeliveryPartnerOtpEntry.builder().phone(phone).build());

            if (entry.getLockedUntil() != null && entry.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AuthException("Too many failed attempts. Please try again later.");
            }

            // Phase 2 — replace this block: generate random OTP, BCrypt-hash it, send via SMS gateway
            entry.setOtpHash(HARDCODED_OTP);
            entry.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            entry.setAttemptCount(0);
            entry.setLockedUntil(null);
            otpRepository.save(entry);
            log.info("sendOtp: end, phone={}", phone);
        } catch (Exception e) {
            log.error("sendOtp: failed, phone={}", phone, e);
            throw e;
        }
    }

    // ── Verify OTP (FR-1.6 — FR-1.10) ────────────────────────────────────────

    @Override
    // noRollbackFor: a wrong-OTP attempt still throws AuthException to fail the
    // request, but the attemptCount/lockedUntil mutation saved just before that
    // throw must survive — otherwise it's discarded by Spring's default
    // rollback-on-RuntimeException and FR-1.9's lockout never engages.
    @Transactional(noRollbackFor = AuthException.class)
    public PartnerAuthResponse verifyOtp(String phone, String otp) {
        log.info("verifyOtp: start, phone={}", phone);
        try {
            DeliveryPartnerOtpEntry entry = otpRepository.findByPhone(phone)
                    .orElseThrow(() -> new AuthException("No OTP found. Please request a new OTP."));

            if (entry.getLockedUntil() != null && entry.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AuthException("Account locked due to too many attempts. Try again in " + LOCKOUT_MINUTES + " minutes.");
            }

            if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
                otpRepository.delete(entry);
                throw new AuthException("OTP has expired. Please request a new one.");
            }

            // Phase 2 — replace plain equality with: passwordEncoder.matches(otp, entry.getOtpHash())
            if (!HARDCODED_OTP.equals(otp)) {
                entry.setAttemptCount(entry.getAttemptCount() + 1);
                if (entry.getAttemptCount() >= MAX_ATTEMPTS) {
                    entry.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                }
                otpRepository.save(entry);
                throw new AuthException("Incorrect OTP. Please try again.");
            }

            otpRepository.delete(entry);

            // A bare record is created on first verification (registrationComplete = false)
            // so the M2 registration flow has a partner id to attach documents to.
            // FR-1.10: isNewUser drives routing to Registration; lifecycleState only
            // matters once registrationComplete is true.
            boolean isNewUser = !partnerRepository.existsByPhone(phone);
            DeliveryPartner partner = partnerRepository.findByPhone(phone)
                    .orElseGet(() -> partnerRepository.save(DeliveryPartner.builder().phone(phone).build()));

            String accessToken  = jwtService.generateAccessToken(partner);
            String refreshToken = jwtService.generateRefreshToken(partner);
            persistRefreshToken(partner, refreshToken);

            PartnerAuthResponse response = PartnerAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .newUser(isNewUser)
                    .lifecycleState(partner.getLifecycleState())
                    .build();
            log.info("verifyOtp: end, phone={}", phone);
            return response;
        } catch (Exception e) {
            log.error("verifyOtp: failed, phone={}", phone, e);
            throw e;
        }
    }

    // ── Refresh Token (FR-1.12) ──────────────────────────────────────────────

    @Override
    @Transactional
    public PartnerAuthResponse refreshToken(String tokenStr) {
        log.info("refreshToken: start");
        try {
            DeliveryPartnerRefreshToken stored = refreshTokenRepository.findByToken(tokenStr)
                    .orElseThrow(() -> new AuthException("Invalid refresh token"));

            if (stored.isRevoked()) {
                throw new AuthException("Refresh token has been revoked");
            }
            if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AuthException("Refresh token has expired");
            }

            DeliveryPartner partner = stored.getDeliveryPartner();
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);

            String newAccessToken  = jwtService.generateAccessToken(partner);
            String newRefreshToken = jwtService.generateRefreshToken(partner);
            persistRefreshToken(partner, newRefreshToken);

            PartnerAuthResponse response = PartnerAuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .newUser(!partner.isRegistrationComplete())
                    .lifecycleState(partner.getLifecycleState())
                    .build();
            log.info("refreshToken: end, partnerId={}", partner.getId());
            return response;
        } catch (Exception e) {
            log.error("refreshToken: failed", e);
            throw e;
        }
    }

    // ── Logout (FR-1.13) ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String tokenStr) {
        log.info("logout: start");
        try {
            DeliveryPartnerRefreshToken stored = refreshTokenRepository.findByToken(tokenStr).orElse(null);
            if (stored == null) {
                log.info("logout: end, no matching token found");
                return; // already logged out / unknown token — idempotent no-op
            }

            if (stored.getDeliveryPartner().getDutyStatus() != DutyStatus.OFFLINE) {
                throw new AuthException("Go OFFLINE before logging out.");
            }

            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            log.info("logout: end");
        } catch (Exception e) {
            log.error("logout: failed", e);
            throw e;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void persistRefreshToken(DeliveryPartner partner, String tokenStr) {
        DeliveryPartnerRefreshToken rt = DeliveryPartnerRefreshToken.builder()
                .deliveryPartner(partner)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(rt);
    }
}
