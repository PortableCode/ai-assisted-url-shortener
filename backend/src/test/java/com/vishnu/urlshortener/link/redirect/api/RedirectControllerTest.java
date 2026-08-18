package com.vishnu.urlshortener.link.redirect.api;

import com.vishnu.urlshortener.link.redirect.application.RedirectService;
import com.vishnu.urlshortener.link.redirect.application.ShortCodeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private RedirectService redirectService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RedirectController(redirectService)).build();
    }

    @Test
    void redirectReturns302FoundWithLocation() throws Exception {
        when(redirectService.redirect("abc1234")).thenReturn("https://example.com/landing");

        mockMvc.perform(get("/abc1234").accept(MediaType.ALL))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/landing"));
    }

    @Test
    void redirectReturns404ForUnknownCode() throws Exception {
        when(redirectService.redirect("abc1234")).thenThrow(new ShortCodeNotFoundException("abc1234"));

        mockMvc.perform(get("/abc1234").accept(MediaType.ALL))
                .andExpect(status().isNotFound());
    }
}
