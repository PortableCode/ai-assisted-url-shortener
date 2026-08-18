package com.vishnu.urlshortener.common.config;

import com.vishnu.urlshortener.link.api.CreateLinkRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final CreateLinkRateLimitInterceptor createLinkRateLimitInterceptor;

    public WebMvcConfiguration(CreateLinkRateLimitInterceptor createLinkRateLimitInterceptor) {
        this.createLinkRateLimitInterceptor = createLinkRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createLinkRateLimitInterceptor);
    }
}
