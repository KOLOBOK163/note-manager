package com.ksbk.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    public JwtSuccessHandler(JwtUtils jwtUtils)
    {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        String subject = user.getAttribute("sub");

        Map<String, Object> claims = Map.of("email", email,"name", name);

        String token = jwtUtils.generateAccessToken(subject, claims);

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), Map.of("token", token));
    }
}
