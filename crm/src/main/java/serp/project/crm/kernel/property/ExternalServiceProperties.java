package serp.project.crm.kernel.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

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
        if (services != null && !services.isEmpty()) {
            for (ServiceProperties service : services) {
                if (service.getName().equalsIgnoreCase(serviceName)) {
                    return service.getUrl();
                }
            }
        }
        return null;
    }
}
