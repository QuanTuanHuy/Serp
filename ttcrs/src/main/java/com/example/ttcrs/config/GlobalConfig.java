package com.example.ttcrs.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Data
@Configuration
public class GlobalConfig {

    // Security
    @Value("${security.roles.serp_service}")
    private String serpServiceRole;

    @Value("${security.role-prefix}")
    private String rolePrefix;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
