package com.vishnu.urlshortener.link.redirect.api;

import com.vishnu.urlshortener.link.redirect.application.RedirectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final RedirectService redirectService;

    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @Operation(summary = "Redirect to the original URL")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirected to the original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = redirectService.redirect(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
