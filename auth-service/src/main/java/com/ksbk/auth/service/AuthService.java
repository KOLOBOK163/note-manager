package com.ksbk.auth.service;

import com.ksbk.auth.DTO.UserDTO;
import com.ksbk.auth.entity.UserDetailsImpl;
import com.ksbk.auth.DTO.JwtResponse;
import com.ksbk.auth.entity.User;
import com.ksbk.auth.entity.UserRole;
import com.ksbk.auth.exception.InvalidRefreshTokenException;
import com.ksbk.auth.exception.InvalidResetTokenException;
import com.ksbk.auth.exception.UserAlreadyExistException;
import com.ksbk.auth.exception.UserEmailAlreadyExistException;
import com.ksbk.auth.mapper.UserMapper;
import com.ksbk.auth.repository.UserRepository;
import com.ksbk.auth.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final EmailService emailService;

    public AuthService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils, EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    public User signUp(UserDTO userDTO) throws UserAlreadyExistException, UserEmailAlreadyExistException {
        logger.info("Attempting to register new user: {}", userDTO.getUsername());

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Registration failed - username already exists: {}", userDTO.getUsername());
            throw new UserAlreadyExistException("User with this name already exists");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.warn("Registration failed - email already exists: {}", userDTO.getEmail());
            throw new UserEmailAlreadyExistException("User with this email already exists");
        }

        try {
            User user = userMapper.UserDTOToEntity(userDTO);
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setRole(UserRole.USER);

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    public JwtResponse signIn(UserDTO userDTO) {
        logger.debug("Attempting to authenticate user: {}", userDTO.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getUsername(),
                            userDTO.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();

            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpiry(
                    LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpiration() / 1000)
            );
            userRepository.save(user);

            JwtResponse jwtResponse = new JwtResponse();
            jwtResponse.setAccessToken(accessToken);
            jwtResponse.setRefreshToken(refreshToken);
            jwtResponse.setUsername(user.getUsername());
            jwtResponse.setEmail(user.getEmail());
            jwtResponse.setRoles(Collections.singletonList(user.getRole().name()));

            logger.info("User authenticated successfully: {}", user.getUsername());
            return jwtResponse;
        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", userDTO.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public JwtResponse refreshToken(String refreshToken) {
        logger.debug("Attempting to refresh token");

        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            logger.warn("Invalid refresh token provided");
            throw new RuntimeException("Invalid Refresh Token");
        }

        try {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            logger.debug("Processing refresh token for user: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found during token refresh: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });

            if (!refreshToken.equals(user.getRefreshToken())) {
                logger.warn("Refresh token mismatch for user: {}", username);
                throw new InvalidRefreshTokenException("Refresh Token invalid");
            }

            if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Expired refresh token for user: {}", username);
                throw new InvalidRefreshTokenException("Refresh Token expired");
            }

            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails.getUsername(),
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String newAccessToken = jwtUtils.generateAccessToken(authentication);
            String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

            user.setRefreshToken(newRefreshToken);
            user.setRefreshTokenExpiry(
                    LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpiration() / 1000)
            );
            userRepository.save(user);

            JwtResponse jwtResponse = new JwtResponse();
            jwtResponse.setAccessToken(newAccessToken);
            jwtResponse.setRefreshToken(newRefreshToken);
            jwtResponse.setUsername(user.getUsername());
            jwtResponse.setEmail(user.getEmail());
            jwtResponse.setRoles(Collections.singletonList(user.getRole().name()));

            logger.info("Tokens refreshed successfully for user: {}", username);
            return jwtResponse;
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("Password reset requested for non-existent email: {}", email);
                        return new UsernameNotFoundException("User Not Found");
                    });

            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            String resetToken = jwtUtils.generateResetToken(authentication);

            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            sendEmailReset(email, resetToken);
            logger.info("Password reset token generated and sent to: {}", email);
        } catch (Exception e) {
            logger.error("Error requesting password reset for {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    public void resetPassword(String resetToken, String newPassword) {
        logger.debug("Attempting password reset with token");

        if (!jwtUtils.validateResetToken(resetToken)) {
            logger.warn("Invalid reset token provided");
            throw new InvalidResetTokenException("Invalid reset token");
        }

        try {
            String email = jwtUtils.getEmailFromJwtToken(resetToken);
            logger.debug("Processing password reset for email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("User not found during password reset: {}", email);
                        return new UsernameNotFoundException("User not found");
                    });

            if (!resetToken.equals(user.getResetToken())) {
                logger.warn("Reset token mismatch for user: {}", email);
                throw new InvalidResetTokenException("Invalid reset token");
            }

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Expired reset token for user: {}", email);
                throw new InvalidResetTokenException("Reset token expired");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);

            userRepository.save(user);
            logger.info("Password reset successfully for user: {}", email);
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void sendEmailReset(String email, String token) {
        logger.debug("Sending password reset email to: {}", email);

        try {
            String subject = "Password Reset Request for Your Account";
            String resetLink = "https://yourfrontend.com/reset-password?token=" + token;

            String body = "Password Reset Request\n" +
                    "----------------------\n\n" +
                    "We received a request to reset your password for your account.\n\n" +
                    "To reset your password, please visit:\n" +
                    resetLink + "\n\n" +
                    "Or make an API request:\n" +
                    "POST https://yourapi.com/api/auth/reset-password\n" +
                    "Content-Type: application/json\n" +
                    "Body: {\"token\":\"" + token + "\", \"newPassword\":\"your_new_password\"}\n\n" +
                    "Important:\n" +
                    "- This link will expire in 1 hour\n" +
                    "- Never share this token with anyone\n" +
                    "- If you didn't request this, please ignore this email\n\n" +
                    "Thank you,\n" +
                    "Your Application Team";

            emailService.sendMail(email, subject, body);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
}


