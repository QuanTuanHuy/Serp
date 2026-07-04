package serp.project.school_bus_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolBusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Bus Service API")
                        .version("0.1.0")
                        .description("Tenant-aware school bus operations APIs for SERP"));
    }
}
