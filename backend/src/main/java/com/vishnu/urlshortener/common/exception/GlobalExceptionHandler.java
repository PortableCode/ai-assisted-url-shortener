package com.vishnu.urlshortener.common.exception;

import com.vishnu.urlshortener.link.application.InvalidOriginalUrlException;
import com.vishnu.urlshortener.link.application.InvalidExpirationException;
import com.vishnu.urlshortener.link.application.LinkExpiredException;
import com.vishnu.urlshortener.link.application.LinkNotFoundException;
import com.vishnu.urlshortener.link.application.RateLimitExceededException;
import com.vishnu.urlshortener.link.application.ShortCodeGenerationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidOriginalUrlException.class)
    public ProblemDetail handleInvalidOriginalUrl(InvalidOriginalUrlException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ProblemDetail handleLinkNotFound(LinkNotFoundException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(LinkExpiredException.class)
    public ProblemDetail handleLinkExpired(LinkExpiredException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.GONE, "Gone", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Request validation failed.",
                request.getRequestURI()
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request.", request.getRequestURI());
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ProblemDetail handleShortCodeGeneration(ShortCodeGenerationException ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unable to generate a short code.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "Rate limit exceeded.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidExpirationException.class)
    public ProblemDetail handleInvalidExpiration(InvalidExpirationException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(instance));
        return problemDetail;
    }
}
