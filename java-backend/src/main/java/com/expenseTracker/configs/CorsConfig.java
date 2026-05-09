package com.expenseTracker.configs;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration is now consolidated in WebMvcConfig
 * This class is kept for reference and future use
 * @deprecated CORS is configured in WebMvcConfig
 */
@Configuration
@Deprecated(forRemoval = false)
public class CorsConfig {
    // CORS configuration moved to WebMvcConfig to consolidate configuration
}

