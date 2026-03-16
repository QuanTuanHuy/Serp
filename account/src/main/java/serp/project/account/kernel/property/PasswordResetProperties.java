/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {
    private String frontendResetUrl = "http://localhost:3000/auth/reset-password";
    private Long expirationMinutes = 60L;
}
