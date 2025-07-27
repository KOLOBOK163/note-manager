package com.ksbk.notes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class noteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(noteServiceApplication.class, args);
    }
}
