package com.example.aquaventure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generatedToken(String email) {
        Date now = new Date(System.currentTimeMillis());
        SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(
                    new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(signingKey)
                .compact();
    }
}
