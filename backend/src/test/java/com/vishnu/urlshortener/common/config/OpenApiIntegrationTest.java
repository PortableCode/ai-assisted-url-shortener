package com.vishnu.urlshortener.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OpenApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("url_shortener")
            .withUsername("url_shortener")
            .withPassword("url_shortener");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void openApiDocsExposeConfiguredMetadataAndExistingEndpoints() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/v3/api-docs");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"title\":\"AI-Assisted URL Shortener API\""));
        assertTrue(response.body().contains("\"version\":\"v1\""));
        assertTrue(response.body().contains("\"/api/v1/links\""));
        assertTrue(response.body().contains("\"/api/v1/links/{shortCode}\""));
        assertTrue(response.body().contains("\"/api/v1/links/{shortCode}/analytics\""));
        assertTrue(response.body().contains("\"/{shortCode}\""));
    }

    @Test
    void swaggerUiIsServed() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/swagger-ui/index.html");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Swagger UI"));
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
