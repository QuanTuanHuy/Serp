package com.example.ttcrs.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ttcrs")
@Data
public class AppProperties {
    private Long driverRoleId;
}
