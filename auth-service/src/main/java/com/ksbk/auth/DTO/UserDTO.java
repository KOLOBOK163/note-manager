package com.ksbk.auth.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private String avatarUrl;
}
