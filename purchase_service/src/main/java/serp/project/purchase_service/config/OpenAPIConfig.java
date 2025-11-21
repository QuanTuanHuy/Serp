package serp.project.purchase_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "Purchase Service API", version = "0.0.1", description = "API documentation for Purchase Service"), security = @SecurityRequirement(name = "Bearer Authentication"))
@SecurityScheme(name = "Bearer Authentication", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER, description = "Authorization header with Bearer token")
public class OpenAPIConfig {

}
