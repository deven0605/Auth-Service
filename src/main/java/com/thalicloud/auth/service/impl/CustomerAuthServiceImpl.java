package com.thalicloud.auth.service.impl;

import com.thalicloud.auth.dto.response.CustomerAuthResponse;
import com.thalicloud.auth.entity.Customer;
import com.thalicloud.auth.entity.CustomerOtpEntry;
import com.thalicloud.auth.entity.CustomerRefreshToken;
import com.thalicloud.auth.exception.AuthException;
import com.thalicloud.auth.repository.CustomerOtpEntryRepository;
import com.thalicloud.auth.repository.CustomerRefreshTokenRepository;
import com.thalicloud.auth.repository.CustomerRepository;
import com.thalicloud.auth.service.CustomerAuthService;
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
public class CustomerAuthServiceImpl implements CustomerAuthService {

    // Phase 1: SMS gateway bypassed — hardcoded OTP (FR-1.5)
    private static final String HARDCODED_OTP    = "1234";
    private static final int    MAX_ATTEMPTS      = 3;    // FR-1.9
    private static final int    LOCKOUT_MINUTES   = 10;   // FR-1.9
    private static final int    OTP_EXPIRY_MINUTES = 5;   // FR-1.7

    private final CustomerRepository             customerRepository;
    private final CustomerOtpEntryRepository     otpRepository;
    private final CustomerRefreshTokenRepository refreshTokenRepository;
    private final JwtService                     jwtService;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // ── Send OTP (FR-1.4 / FR-1.5) ───────────────────────────────────────────

    @Override
    @Transactional
    public void sendOtp(String phone) {
        log.info("sendOtp: start, phone={}", phone);
        try {
            CustomerOtpEntry entry = otpRepository.findByPhone(phone)
                    .orElse(CustomerOtpEntry.builder().phone(phone).build());

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

    // ── Verify OTP (FR-1.6 — FR-1.9) ─────────────────────────────────────────

    @Override
    // noRollbackFor: a wrong-OTP attempt still throws AuthException to fail the
    // request, but the attemptCount/lockedUntil mutation saved just before that
    // throw must survive — otherwise it's discarded by Spring's default
    // rollback-on-RuntimeException and FR-1.9's lockout never engages.
    @Transactional(noRollbackFor = AuthException.class)
    public CustomerAuthResponse verifyOtp(String phone, String otp) {
        log.info("verifyOtp: start, phone={}", phone);
        try {
            CustomerOtpEntry entry = otpRepository.findByPhone(phone)
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

            boolean isNewUser = !customerRepository.existsByPhone(phone);
            Customer customer = customerRepository.findByPhone(phone)
                    .orElseGet(() -> customerRepository.save(Customer.builder().phone(phone).build()));

            String accessToken  = jwtService.generateAccessToken(customer);
            String refreshToken = jwtService.generateRefreshToken(customer);
            persistRefreshToken(customer, refreshToken);

            CustomerAuthResponse response = CustomerAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .newUser(isNewUser)
                    .build();
            log.info("verifyOtp: end, phone={}", phone);
            return response;
        } catch (Exception e) {
            log.error("verifyOtp: failed, phone={}", phone, e);
            throw e;
        }
    }

    // ── Refresh Token (FR-1.15) ───────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerAuthResponse refreshToken(String tokenStr) {
        log.info("refreshToken: start");
        try {
            CustomerRefreshToken stored = refreshTokenRepository.findByToken(tokenStr)
                    .orElseThrow(() -> new AuthException("Invalid refresh token"));

            if (stored.isRevoked()) {
                throw new AuthException("Refresh token has been revoked");
            }
            if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AuthException("Refresh token has expired");
            }

            Customer customer = stored.getCustomer();
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);

            String newAccessToken  = jwtService.generateAccessToken(customer);
            String newRefreshToken = jwtService.generateRefreshToken(customer);
            persistRefreshToken(customer, newRefreshToken);

            CustomerAuthResponse response = CustomerAuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .newUser(false)
                    .build();
            log.info("refreshToken: end, customerId={}", customer.getId());
            return response;
        } catch (Exception e) {
            log.error("refreshToken: failed", e);
            throw e;
        }
    }

    // ── Logout (FR-1.16) ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String tokenStr) {
        log.info("logout: start");
        try {
            refreshTokenRepository.findByToken(tokenStr).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
            log.info("logout: end");
        } catch (Exception e) {
            log.error("logout: failed", e);
            throw e;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void persistRefreshToken(Customer customer, String tokenStr) {
        CustomerRefreshToken rt = CustomerRefreshToken.builder()
                .customer(customer)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(rt);
    }
}
