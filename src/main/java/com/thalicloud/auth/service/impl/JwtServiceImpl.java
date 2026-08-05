package com.thalicloud.auth.service.impl;

import com.thalicloud.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        log.info("generateAccessToken: start, username={}", userDetails.getUsername());
        try {
            String token = buildToken(Map.of("type", "access"), userDetails, accessTokenExpiryMs);
            log.info("generateAccessToken: end, username={}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("generateAccessToken: failed, username={}", userDetails.getUsername(), e);
            throw e;
        }
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        log.info("generateRefreshToken: start, username={}", userDetails.getUsername());
        try {
            String token = buildToken(Map.of("type", "refresh"), userDetails, refreshTokenExpiryMs);
            log.info("generateRefreshToken: end, username={}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("generateRefreshToken: failed, username={}", userDetails.getUsername(), e);
            throw e;
        }
    }

    @Override
    public String extractUsername(String token) {
        log.info("extractUsername: start");
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.info("extractUsername: end, username={}", username);
            return username;
        } catch (Exception e) {
            log.error("extractUsername: failed", e);
            throw e;
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("isTokenValid: start, username={}", userDetails.getUsername());
        try {
            boolean valid = extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
            log.info("isTokenValid: end, username={}, valid={}", userDetails.getUsername(), valid);
            return valid;
        } catch (Exception e) {
            log.error("isTokenValid: failed, username={}", userDetails.getUsername(), e);
            throw e;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        log.info("isTokenExpired: start");
        try {
            boolean expired = extractClaim(token, Claims::getExpiration).before(new Date());
            log.info("isTokenExpired: end, expired={}", expired);
            return expired;
        } catch (Exception e) {
            log.error("isTokenExpired: failed", e);
            throw e;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiryMs) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
