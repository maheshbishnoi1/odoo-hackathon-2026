package com.odoo.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for TransitOps backend.
 * <p>
 * Accessible at: {@code /swagger-ui.html} and {@code /v3/api-docs}
 * </p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "TransitOps API",
                version     = "v1.0",
                description = "Fleet & Transport Operations Management System — REST API documentation",
                contact     = @Contact(name = "TransitOps Team")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local Development"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name        = "bearerAuth",
        type        = SecuritySchemeType.HTTP,
        scheme      = "bearer",
        bearerFormat = "JWT",
        in          = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is handled entirely through annotations
}
