package com.ksbk.auth.controller;



import com.ksbk.auth.DTO.PasswordResetRequest;
import com.ksbk.auth.DTO.UserDTO;
import com.ksbk.auth.DTO.JwtResponse;
import com.ksbk.auth.service.AuthService;
import com.ksbk.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "The authentication controller allows you to make basic methods with the account operation")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User registration")
    @PostMapping("/register")
    public ResponseEntity<String> signUp(@RequestBody UserDTO userDTO){
        logger.info("Registration request for user: {}", userDTO.getUsername());
        try {
            User regUser = authService.signUp(userDTO);
            logger.info("User {} was registered successfully with email: {}", regUser.getUsername(), regUser.getEmail());
            return ResponseEntity.ok("User was saved successfully");
        } catch (Exception e){
            logger.error("Registration failed for user {}: {}", userDTO.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody UserDTO userDTO){
        logger.debug("Login attempt for user: {}", userDTO.getUsername());
        try{
            JwtResponse jwtResponse = authService.signIn(userDTO);
            logger.info("User {} logged in successfully.", jwtResponse.getUsername());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e){
            logger.warn("Login failed for user {}: {}", userDTO.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update refresh-token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken){
        logger.debug("Refresh token request received");
        try{
            JwtResponse jwtResponse = authService.refreshToken(refreshToken);
            logger.info("For {} generated a new refresh token", jwtResponse.getUsername());
            return ResponseEntity.ok(jwtResponse);
        }catch (Exception e)
        {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Password update request")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequest request)
    {
        logger.info("Password reset requested for email: {}", request.getEmail());
        try {
            authService.requestPasswordReset(request.getEmail());
            logger.info("Password reset link was send to {}", request.getEmail());
            return ResponseEntity.ok("Password reset link was sent to email");
        }catch (Exception e)
        {
            logger.error("Failed to send password reset to {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

    @Operation(summary = "Update password")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request)
    {
        logger.info("Password reset attempt with token");
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            logger.info("Password reset successfully");
            return ResponseEntity.ok("Password has been reset was successfully");
        } catch (Exception e)
        {
            logger.error("Failed password reset: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
