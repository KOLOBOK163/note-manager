package com.ksbk.auth.controller;

import com.ksbk.auth.DTO.UserDTO;
import com.ksbk.auth.entity.User;
import com.ksbk.auth.DTO.UserResponse;
import com.ksbk.auth.repository.UserRepository;
import com.ksbk.auth.service.MinioService;
import com.ksbk.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/api/user")
@Tag(name = "Custom Controller", description = "User controller allows you to get, update, delete a user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    private final MinioService minioService;

    private final UserRepository userRepository;

    public UserController(UserService userService, MinioService minioService, UserRepository userRepository) {
        this.userService = userService;
        this.minioService = minioService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get user", description = "Get user by id")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        try{
            User user = userService.getUserById(userId);
            UserResponse dto = new UserResponse();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setAvatarUrl(user.getAvatarUrl());
            logger.info("Retrieved user with id: {}, username: {}", userId, user.getUsername());
            return ResponseEntity.ok(dto);
        }catch (Exception e)
        {
            logger.error("Failed to get user with id {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update user", description = "Update user by id")
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUserById(@RequestBody UserDTO userDTO, @PathVariable Long userId) {
        try{
            User user = userService.updateUser(userDTO, userId);
            logger.info("User {} (id: {}) updated successfully.", userDTO.getUsername(), userId);
            return ResponseEntity.ok(user);
        }catch (Exception e)
        {
            logger.error("Failed to update user with id {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete user", description = "Delete user by id")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId){
        try{
            userService.deleteUser(userId);
            logger.info("User was with id {} deleted successfully", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e)
        {
            logger.error("Failed to delete user with id {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers()
    {
        try{
            List<UserResponse> users = userService.getAllUser();
            logger.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        }catch (Exception e)
        {
            logger.error("Failed to get all users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Upload avatar", description = "Upload user avatar image")
    @PostMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "Avatar file", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws Exception {
        try
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> {
                logger.error("User not found with username: {}", currentUsername);
                return new UsernameNotFoundException("User not found with username: " + currentUsername);
            });
            Long userId = currentUser.getId();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String objectName = String.format("avatars/user_%d_%s%s", userId, timestamp, extension);

            String avatarUrl = minioService.uploadAvatar(file, objectName);
            userService.updateAvatar(avatarUrl);
            logger.info("User {} (id: {}) changed avatar to {}", currentUser.getUsername(), userId, avatarUrl);
            return ResponseEntity.ok("Avatar was uploaded successfully");
        }catch (Exception e)
        {
            logger.error("Upload avatar failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get avatar", description = "Get user avatar image")
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        try {
            String objectName = userService.getAvatar(userId);
            logger.info("Avatar {} of user with id: {}", objectName, userId);
            if (objectName == null || objectName.isEmpty()) {
                logger.warn("Avatar not found for user with id: {}", userId);
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User has no avatar");
            }

            logger.debug("Fetching avatar {} for user {}", objectName, userId);
            try (InputStream is = minioService.getAvatar(objectName)) {
                String contentType = URLConnection.guessContentTypeFromName(objectName);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                byte[] bytes = is.readAllBytes();
                logger.info("Successfully retrieved avatar for user {}", userId);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(bytes);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching avatar for user {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to fetch avatar: " + e.getMessage());
        }
    }
}
