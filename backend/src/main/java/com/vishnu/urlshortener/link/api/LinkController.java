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
