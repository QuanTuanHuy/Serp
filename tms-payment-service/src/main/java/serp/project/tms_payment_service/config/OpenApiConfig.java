package serp.project.tms_payment_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Service API",
                version = "1.0",
                description = "API quản lý thanh toán và tích hợp với ZaloPay",
                contact = @Contact(
                        name = "CN WEB Team",
                        email = "support@cnweb.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8096", description = "Local Development Server"),
                @Server(url = "http://localhost:8080/payment/api/v1", description = "API Gateway (recommended)")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}
