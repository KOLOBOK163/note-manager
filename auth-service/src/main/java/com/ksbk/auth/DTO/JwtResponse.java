package com.ksbk.auth.DTO;

import lombok.Data;

import java.util.Collection;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private Collection<String> roles;
}
