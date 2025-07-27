package com.ksbk.notes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "NoteManager",
                description = "Notes System API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Vladislav Kalinin",
                        email = "LILKSBK@yandex.ru"
                )
        )
)

@Configuration
public class ApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Notes API").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
