package com.vishnu.urlshortener.link.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishnu.urlshortener.common.exception.GlobalExceptionHandler;
import com.vishnu.urlshortener.link.application.InvalidExpirationException;
import com.vishnu.urlshortener.link.application.InvalidOriginalUrlException;
import com.vishnu.urlshortener.link.application.LinkNotFoundException;
import com.vishnu.urlshortener.link.application.LinkService;
import com.vishnu.urlshortener.link.application.ShortCodeGenerationException;
import com.vishnu.urlshortener.link.domain.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LinkControllerTest {

    @Mock
    private LinkService linkService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new LinkController(linkService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createLinkReturnsCreatedResponse() throws Exception {
        Instant createdAt = Instant.parse("2026-08-17T23:35:00Z");
        when(linkService.createLink("https://example.com/landing", null))
                .thenReturn(new Link("abc1234", "https://example.com/landing", createdAt));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Host", "sho.rt")
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("https://example.com/landing"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://sho.rt/abc1234"))
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://sho.rt/abc1234"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/landing"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-17T23:35:00Z"))
                .andExpect(jsonPath("$.expiresAt").value(nullValue()));
    }

    @Test
    void createLinkReturnsCreatedResponseWithExpiration() throws Exception {
        Instant createdAt = Instant.parse("2026-08-17T23:35:00Z");
        Instant expiresAt = Instant.parse("2026-08-18T00:35:00Z");
        when(linkService.createLink("https://example.com/landing", expiresAt))
                .thenReturn(new Link("abc1234", "https://example.com/landing", createdAt, expiresAt));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Host", "sho.rt")
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("https://example.com/landing", expiresAt))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").value("2026-08-18T00:35:00Z"));
    }

    @Test
    void metadataReturnsStoredFields() throws Exception {
        Instant createdAt = Instant.parse("2026-08-17T23:35:00Z");
        Instant expiresAt = Instant.parse("2026-08-18T00:35:00Z");
        when(linkService.getLinkMetadata("abc1234"))
                .thenReturn(new Link("abc1234", "https://example.com/landing", createdAt, expiresAt));

        mockMvc.perform(get("/api/v1/links/abc1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/landing"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-17T23:35:00Z"))
                .andExpect(jsonPath("$.expiresAt").value("2026-08-18T00:35:00Z"));
    }

    @Test
    void analyticsReturnsStoredFields() throws Exception {
        Link link = new Link("abc1234", "https://example.com/landing", Instant.parse("2026-08-17T23:35:00Z"));
        when(linkService.getLinkAnalytics("abc1234")).thenReturn(link);

        mockMvc.perform(get("/api/v1/links/abc1234/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.lastAccessedAt").value(nullValue()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/links/abc1234"))
                .andExpect(status().isNoContent());
    }

    @Test
    void metadataReturnsNotFoundForUnknownCode() throws Exception {
        when(linkService.getLinkMetadata("abc1234")).thenThrow(new LinkNotFoundException("abc1234"));

        mockMvc.perform(get("/api/v1/links/abc1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Short code not found: abc1234"))
                .andExpect(jsonPath("$.instance").value("/api/v1/links/abc1234"));
    }

    @Test
    void analyticsReturnsNotFoundForUnknownCode() throws Exception {
        when(linkService.getLinkAnalytics("abc1234")).thenThrow(new LinkNotFoundException("abc1234"));

        mockMvc.perform(get("/api/v1/links/abc1234/analytics"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Short code not found: abc1234"))
                .andExpect(jsonPath("$.instance").value("/api/v1/links/abc1234/analytics"));
    }

    @Test
    void deleteReturnsNotFoundForUnknownCode() throws Exception {
        doThrow(new LinkNotFoundException("abc1234")).when(linkService).deleteLink("abc1234");

        mockMvc.perform(delete("/api/v1/links/abc1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Short code not found: abc1234"))
                .andExpect(jsonPath("$.instance").value("/api/v1/links/abc1234"));
    }

    @Test
    void createLinkReturnsBadRequestWhenOriginalUrlIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Request validation failed."))
                .andExpect(jsonPath("$.instance").value("/api/v1/links"))
                .andExpect(jsonPath("$.errors.originalUrl").value("originalUrl must not be blank"));

        verifyNoInteractions(linkService);
    }

    @Test
    void createLinkReturnsBadRequestProblemDetailForInvalidOriginalUrl() throws Exception {
        when(linkService.createLink("ftp://example.com", null)).thenThrow(new InvalidOriginalUrlException("originalUrl must use http or https"));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("ftp://example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("originalUrl must use http or https"))
                .andExpect(jsonPath("$.instance").value("/api/v1/links"));
    }

    @Test
    void createLinkReturnsBadRequestProblemDetailForInvalidExpiration() throws Exception {
        Instant expiresAt = Instant.parse("2026-08-17T23:30:00Z");
        when(linkService.createLink("https://example.com", expiresAt))
                .thenThrow(new InvalidExpirationException("expiresAt must be in the future"));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("https://example.com", expiresAt))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("expiresAt must be in the future"));
    }

    @Test
    void createLinkReturnsBadRequestProblemDetailForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Malformed JSON request."))
                .andExpect(jsonPath("$.instance").value("/api/v1/links"));
    }

    @Test
    void createLinkReturnsInternalServerErrorProblemDetailForShortCodeGenerationFailure() throws Exception {
        when(linkService.createLink("https://example.com", null)).thenThrow(new ShortCodeGenerationException("Unable to generate a unique short code after 5 attempts", null));

        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("https://example.com"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Unable to generate a short code."))
                .andExpect(jsonPath("$.instance").value("/api/v1/links"));
    }
}
