package com.ksbk.notes.service;

import com.ksbk.notes.DTO.UserResponse;
import com.ksbk.notes.config.FeignClientInterceptorConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "http://auth-service:8082", configuration = FeignClientInterceptorConfig.class)
public interface AuthServiceClient {
    @GetMapping("/api/user/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}