package com.ksbk.auth.DTO;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
}
