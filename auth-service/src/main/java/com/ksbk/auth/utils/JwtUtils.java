package com.ksbk.auth.utils;

import com.ksbk.auth.entity.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwtAccessSecret}")
    private String jwtAccessSecret;

    @Value("${app.jwtAccessExpirationMs}")
    private int jwtAccessExpirationMs;

    @Value("${app.jwtRefreshSecret}")
    private String jwtRefreshSecret;

    @Value("${app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    @Value("${app.jwtResetSecret}")
    private String jwtResetSecret;

    @Value("${app.jwtResetExpirationMs}")
    private int jwtResetExpirationMs;

    private SecretKey accessTokenKey() {
        return Keys.hmacShaKeyFor(jwtAccessSecret.getBytes());
    }
    private SecretKey refreshTokenKey() {return  Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes());}
    private SecretKey resetTokenKey() {return Keys.hmacShaKeyFor(jwtResetSecret.getBytes());}

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("user_id", userPrincipal.getUser().getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtAccessExpirationMs))
                .claim("roles", userPrincipal.getAuthorities())
                .signWith(accessTokenKey())
                .compact();
    }

    public String generateRefreshToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(refreshTokenKey())
                .compact();
    }

    public String generateResetToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtResetExpirationMs))
                .signWith(resetTokenKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(resetTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateAccessToken(@NotNull String accessToken)
    {
        return validateToken(accessToken, accessTokenKey());
    }

    public boolean validateRefreshToken(@NotNull String refreshToken){
        return validateToken(refreshToken, refreshTokenKey());
    }

    public boolean validateResetToken(@NotNull String resetToken){
        return validateToken(resetToken, resetTokenKey());
    }

    public boolean validateToken(String authToken, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        } catch (JwtException e) {
            System.err.println("JWT error: " + e.getMessage());
        }
        return false;
    }


    public long getRefreshExpiration() {
        return jwtRefreshExpirationMs;
    }
}