package com.ksbk.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "NoteManager",
                description = "Notes System API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Vladislav Kalinin",
                        email = "LILKSBK@yandex.ru"
                )
        )
)

public class OpenApiConfig {
}
