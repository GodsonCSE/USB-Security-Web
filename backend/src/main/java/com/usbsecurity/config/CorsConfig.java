package com.usbsecurity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * CORS config: allows the dashboard served from localhost:8080 to call
 * the REST API on the same host.  In a LAN deployment you would restrict
 * this to specific known IPs rather than "*".
 *
 * NOTE: Because authentication is intentionally disabled (local/trusted
 * environment only), do NOT expose this application on a public network.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .allowedHeaders("*");

        registry.addMapping("/sse/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET");
    }
}
