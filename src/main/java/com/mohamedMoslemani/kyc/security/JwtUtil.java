package com.mohamedMoslemani.kyc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            System.out.println("[JWTUTIL] Failed to decode secret: " + e.getMessage());
            throw e;
        }
    }

    // Generate token
    public String generateToken(UserDetails userDetails) {
        return createToken(Map.of(), userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username
    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsername(token);
            boolean expired = isTokenExpired(token);
            System.out.println("[JWTUTIL] Token subject: " + username);
            System.out.println("[JWTUTIL] DB username: " + userDetails.getUsername());
            System.out.println("[JWTUTIL] Token expired: " + expired);

            return username.equals(userDetails.getUsername()) && !expired;
        } catch (Exception e) {
            System.out.println("[JWTUTIL] Validation error: " + e.getMessage());
            return false;
        }
    }

    // Helpers
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
