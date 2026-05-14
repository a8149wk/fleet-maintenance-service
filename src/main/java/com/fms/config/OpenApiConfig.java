package com.fms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc / Swagger configuration. Exposes the mobile REST API as a
 * sandboxed playground at <code>/swagger-ui.html</code> with a single
 * "bearer-jwt" security scheme so callers can paste a token from the
 * <code>POST /api/mobile/auth/login</code> response and call protected
 * endpoints directly from the browser.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetMaintenanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fleet Maintenance Service - Mobile API")
                        .description("REST endpoints consumed by the mobile companion app. "
                                + "All endpoints under /api/** (except /api/mobile/auth/**) require "
                                + "a JWT bearer token obtained from the login endpoint.")
                        .version("v1")
                        .contact(new Contact().name("Fleet Maintenance Service"))
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * Group only the mobile-facing REST endpoints. Web/Thymeleaf
     * controllers in {@code com.fms.controller.web} are NOT exposed
     * to Swagger because they return HTML, not JSON.
     */
    @Bean
    public GroupedOpenApi mobileApi() {
        return GroupedOpenApi.builder()
                .group("mobile-api")
                .pathsToMatch("/api/**")
                .packagesToScan("com.fms.controller.api")
                .build();
    }
}
