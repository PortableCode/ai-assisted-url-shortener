package com.vishnu.urlshortener.link.api;

import com.vishnu.urlshortener.link.application.InvalidOriginalUrlException;
import com.vishnu.urlshortener.link.application.LinkService;
import com.vishnu.urlshortener.link.application.ShortCodeGenerationException;
import com.vishnu.urlshortener.link.domain.Link;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateLinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        Link link = linkService.createLink(request.originalUrl());
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/")
                .path(link.getShortCode())
                .toUriString();

        CreateLinkResponse response = new CreateLinkResponse(
                link.getShortCode(),
                shortUrl,
                link.getOriginalUrl(),
                link.getCreatedAt()
        );

        return ResponseEntity.created(URI.create(shortUrl)).body(response);
    }


    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public ResponseEntity<LinkMetadataResponse> getLinkMetadata(@PathVariable String shortCode) {
        Link link = linkService.getLinkMetadata(shortCode);
        return ResponseEntity.ok(new LinkMetadataResponse(link.getShortCode(), link.getOriginalUrl(), link.getCreatedAt()));
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}/analytics")
    public ResponseEntity<LinkAnalyticsResponse> getLinkAnalytics(@PathVariable String shortCode) {
        Link link = linkService.getLinkAnalytics(shortCode);
        return ResponseEntity.ok(new LinkAnalyticsResponse(link.getShortCode(), link.getClickCount(), link.getLastAccessedAt()));
    }

    @DeleteMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode) {
        linkService.deleteLink(shortCode);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(InvalidOriginalUrlException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOriginalUrl(InvalidOriginalUrlException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ProblemDetail> handleShortCodeGenerationFailure(ShortCodeGenerationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
