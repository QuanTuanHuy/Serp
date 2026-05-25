package com.example.ttcrs.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.external")
@Data
public class ExternalServiceProperties {

    private List<ServiceProperties> services;

    @Data
    public static class ServiceProperties {
        private String name;
        private String url;
    }

    public String getServiceUrlByName(String serviceName) {
        if (services == null || services.isEmpty()) return null;
        return services.stream()
                .filter(s -> s.getName().equalsIgnoreCase(serviceName))
                .map(ServiceProperties::getUrl)
                .findFirst()
                .orElse(null);
    }
}
