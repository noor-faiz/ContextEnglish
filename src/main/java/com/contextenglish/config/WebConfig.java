package com.contextenglish.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Reserved for future static-resource / interceptor / CORS configuration.
    // Static assets (css/js/images) are already served automatically from
    // src/main/resources/static by Spring Boot's default resource handling.
}
