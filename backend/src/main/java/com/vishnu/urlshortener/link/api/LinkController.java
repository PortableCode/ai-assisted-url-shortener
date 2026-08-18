package com.vishnu.urlshortener.link.api;

import com.vishnu.urlshortener.link.application.LinkService;
import com.vishnu.urlshortener.link.domain.Link;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/links", produces = MediaType.APPLICATION_JSON_VALUE)
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(summary = "Create a short link")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short link created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "429", description = "Too many create requests"),
            @ApiResponse(responseCode = "500", description = "Short-code generation failed")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateLinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        Link link = linkService.createLink(request.originalUrl(), request.expiresAt());
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/")
                .path(link.getShortCode())
                .toUriString();

        CreateLinkResponse response = new CreateLinkResponse(
                link.getShortCode(),
                shortUrl,
                link.getOriginalUrl(),
                link.getCreatedAt(),
                link.getExpiresAt()
        );

        return ResponseEntity.created(URI.create(shortUrl)).body(response);
    }

    @Operation(summary = "Get link metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link metadata returned"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public ResponseEntity<LinkMetadataResponse> getLinkMetadata(@PathVariable String shortCode) {
        Link link = linkService.getLinkMetadata(shortCode);
        return ResponseEntity.ok(new LinkMetadataResponse(link.getShortCode(), link.getOriginalUrl(), link.getCreatedAt(), link.getExpiresAt()));
    }

    @Operation(summary = "Get aggregate link analytics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link analytics returned"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}/analytics")
    public ResponseEntity<LinkAnalyticsResponse> getLinkAnalytics(@PathVariable String shortCode) {
        Link link = linkService.getLinkAnalytics(shortCode);
        return ResponseEntity.ok(new LinkAnalyticsResponse(link.getShortCode(), link.getClickCount(), link.getLastAccessedAt()));
    }

    @Operation(summary = "Delete a short link")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Link deleted"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @DeleteMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.noContent().build();
    }
}
