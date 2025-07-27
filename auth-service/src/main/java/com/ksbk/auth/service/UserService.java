package com.ksbk.auth.service;

import com.ksbk.auth.DTO.UserDTO;
import com.ksbk.auth.entity.User;
import com.ksbk.auth.DTO.UserResponse;
import com.ksbk.auth.mapper.UserMapper;
import com.ksbk.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final MinioService minioService;

    public UserService(UserRepository userRepository, UserMapper userMapper, MinioService minioService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.minioService = minioService;
    }

    public User getUserById(Long userId) {
        logger.debug("Attempting to get user by id: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found with id: {}", userId);
                        return new UsernameNotFoundException("User not found with id: " + userId);
                    });
            logger.debug("Successfully retrieved user with id: {}", userId);
            return user;
        } catch (Exception e) {
            logger.error("Error getting user by id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public User updateUser(UserDTO userDTO, Long userId) {
        logger.info("Attempting to update user with id: {}, new data: {}", userId, userDTO);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found for update with id: {}", userId);
                        return new UsernameNotFoundException("User not found with id: " + userId);
                    });

            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setAvatarUrl(userDTO.getAvatarUrl());

            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user with id: {}", userId);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user with id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteUser(Long userId) {
        logger.info("Attempting to delete user with id: {}", userId);
        try {
            User user = getUserById(userId);

            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                logger.debug("Attempting to delete avatar for user {} from MinIO", userId);
                try {
                    minioService.deleteAvatar(avatarUrl);
                    logger.debug("Successfully deleted avatar for user {} from MinIO", userId);
                } catch (Exception e) {
                    logger.error("Failed to delete user's avatar from MinIO for user {}: {}",
                            userId, e.getMessage(), e);
                }
            }

            userRepository.delete(user);
            logger.info("Successfully deleted user with id: {}", userId);
        } catch (Exception e) {
            logger.error("Error deleting user with id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public List<UserResponse> getAllUser() {
        logger.debug("Attempting to get all users");
        try {
            List<User> users = userRepository.findAll();
            List<UserResponse> responses = userMapper.UsersEntityToUsersResponse(users);
            logger.info("Successfully retrieved {} users", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Error getting all users: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Transactional
    public void updateAvatar(String newAvatarPath) {
        logger.debug("Attempting to update user avatar");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found for avatar update: {}", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });

            String oldAvatarUrl = user.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                logger.debug("Attempting to delete old avatar from MinIO: {}", oldAvatarUrl);
                try {
                    minioService.deleteAvatar(oldAvatarUrl.trim());
                    logger.debug("Successfully deleted old avatar from MinIO");
                } catch (Exception e) {
                    logger.error("Failed to delete old avatar from MinIO: {}", e.getMessage(), e);
                }
            }

            user.setAvatarUrl(newAvatarPath);
            userRepository.save(user);
            logger.info("Successfully updated avatar for user: {}", username);
        } catch (Exception e) {
            logger.error("Error updating avatar: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String getAvatar(Long userId) {
        logger.debug("Attempting to get avatar for user: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found when getting avatar: {}", userId);
                        return new UsernameNotFoundException("User not found with id: " + userId);
                    });

            String avatarUrl = user.getAvatarUrl();
            logger.debug("Successfully retrieved avatar URL for user {}: {}", userId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            logger.error("Error getting avatar for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}


