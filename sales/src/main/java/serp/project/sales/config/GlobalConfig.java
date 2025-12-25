package serp.project.sales.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class GlobalConfig {

    // Security
    @Value("${security.roles.serp_service}")
    private String serpServiceRole;

    @Value("${security.role-prefix}")
    private String rolePrefix;

}
