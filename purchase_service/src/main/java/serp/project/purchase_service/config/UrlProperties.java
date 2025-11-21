package serp.project.purchase_service.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.urls")
public class UrlProperties {
    List<Pair<String, String>> publicUrls;

    List<ProtectedUrls> protectedUrls;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ProtectedUrls{
        private String urlPattern;
        private List<String> roles;
        private List<String> permissions;
    }
}
