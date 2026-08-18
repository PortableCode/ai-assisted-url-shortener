package com.vishnu.urlshortener.link.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishnu.urlshortener.link.application.LinkService;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createLinkReturnsCreatedResponse() throws Exception {
        Instant createdAt = Instant.parse("2026-08-17T23:35:00Z");
        when(linkService.createLink("https://example.com/landing"))
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
                .andExpect(jsonPath("$.createdAt").value("2026-08-17T23:35:00Z"));
    }

    @Test
    void createLinkReturnsBadRequestWhenOriginalUrlIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLinkRequest("   "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(linkService);
    }
}
