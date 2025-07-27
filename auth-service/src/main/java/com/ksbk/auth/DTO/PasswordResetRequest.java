package com.ksbk.auth.DTO;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String token;
    private String newPassword;
}
