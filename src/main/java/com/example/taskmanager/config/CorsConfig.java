package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allow frontend origin
        config.addAllowedOrigin("http://localhost:5173");

        // ✅ Allow common HTTP methods
        config.addAllowedMethod("*");

        // ✅ Allow all headers (Authorization, Content-Type, etc.)
        config.addAllowedHeader("*");

        // ✅ Allow sending cookies / Authorization headers
        config.setAllowCredentials(true);

        // Apply config to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
