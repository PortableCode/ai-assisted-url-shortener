package com.vishnu.urlshortener.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI urlShortenerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AI-Assisted URL Shortener API")
                .version("v1")
                .description("REST API for creating, redirecting, inspecting, analyzing, and deleting short links."));
    }
}
