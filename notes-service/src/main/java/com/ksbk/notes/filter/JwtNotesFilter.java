package com.ksbk.notes.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class JwtNotesFilter extends OncePerRequestFilter {
    @Value("${app.jwtAccessSecret}")
    private String jwtAccessSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Long userId = getUserIdFromJwtToken(token);
            if(userId != null){
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private Long getUserIdFromJwtToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtAccessSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("user_id", Long.class);
    }

}
