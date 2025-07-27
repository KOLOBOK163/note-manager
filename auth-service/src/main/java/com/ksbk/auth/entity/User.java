package com.ksbk.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    public LocalDateTime refreshTokenExpiry;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
}
