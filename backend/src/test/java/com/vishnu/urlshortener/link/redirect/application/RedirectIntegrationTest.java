package com.vishnu.urlshortener.link.redirect.application;

import com.vishnu.urlshortener.link.application.LinkService;
import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
class RedirectIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("url_shortener")
            .withUsername("url_shortener")
            .withPassword("url_shortener");

    @Autowired
    private LinkService linkService;

    @Autowired
    private RedirectService redirectService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private EntityManager entityManager;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void deletedLinkNoLongerRedirects() throws Exception {
        Link link = new Link("def5678", "https://example.com/deleted", Instant.parse("2026-08-18T00:00:00Z"));
        linkRepository.saveAndFlush(link);
        entityManager.clear();

        linkService.deleteLink("def5678");
        entityManager.clear();

        mockMvc.perform(get("/def5678"))
                .andExpect(status().isNotFound());
    }

    @Test
    void repeatedConcurrentRedirectsIncrementClickCountAtomically() throws Exception {
        Link link = new Link("abc1234", "https://example.com/landing", Instant.parse("2026-08-18T00:00:00Z"));
        linkRepository.saveAndFlush(link);
        entityManager.clear();

        int requests = 16;
        ExecutorService executor = Executors.newFixedThreadPool(requests);
        CountDownLatch ready = new CountDownLatch(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < requests; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(10, TimeUnit.SECONDS);
                return redirectService.redirect("abc1234");
            }));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS));
        start.countDown();

        for (Future<String> future : futures) {
            assertEquals("https://example.com/landing", future.get(5, TimeUnit.SECONDS));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        entityManager.clear();
        Link reloaded = linkRepository.findByShortCode("abc1234").orElseThrow();
        assertEquals(requests, reloaded.getClickCount());
        assertNotNull(reloaded.getLastAccessedAt());
    }
}
