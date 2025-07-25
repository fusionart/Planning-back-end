package com.monbat.planning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")  // Apply to all /api endpoints
                        .allowedOrigins(
                                "http://localhost:5173",    // Vite dev server
                                "http://localhost:3000",    // Alternative dev port
                                "http://127.0.0.1:5173",   // IP version
                                "http://localhost:8080"     // Your frontend port if different
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)  // Set to false for public APIs
                        .maxAge(3600);           // Cache preflight for 1 hour
            }
        };
    }
}
