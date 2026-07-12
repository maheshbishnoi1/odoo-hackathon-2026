package com.odoo.backend.security;

import com.odoo.backend.config.JwtProperties;
import com.odoo.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ============================================================================
 * JWT Service
 * ============================================================================
 *
 * Handles:
 * - JWT Token Generation
 * - JWT Validation
 * - Claim Extraction
 *
 * ============================================================================
 */

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // =========================================================================
    // Generate JWT Token
    // =========================================================================

    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + jwtProperties.getAccessTokenExpiryMs()
                ))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================================================
    // Username
    // =========================================================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // =========================================================================
    // Role
    // =========================================================================

    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("role", String.class));
    }

    // =========================================================================
    // User ID
    // =========================================================================

    public Long extractUserId(String token) {
        return extractClaim(token,
                claims -> claims.get("userId", Long.class));
    }

    // =========================================================================
    // Validate Token
    // =========================================================================

    public boolean isTokenValid(String token,
                                UserDetails userDetails) {

        return extractUsername(token)
                .equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // =========================================================================
    // Expiration
    // =========================================================================

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =========================================================================
    // Generic Claim Extractor
    // =========================================================================

    public <T> T extractClaim(String token,
                              Function<Claims, T> resolver) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    // =========================================================================
    // Signing Key
    // =========================================================================

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

}