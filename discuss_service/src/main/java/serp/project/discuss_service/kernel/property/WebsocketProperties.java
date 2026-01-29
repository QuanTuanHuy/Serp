package serp.project.discuss_service.kernel.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "app.websocket")
@Data
@Component
public class WebsocketProperties {
    private String endpoint;
    private String[] allowedOrigins;
}