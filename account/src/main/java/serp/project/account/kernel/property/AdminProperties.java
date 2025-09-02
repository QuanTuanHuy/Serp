/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.admin")
@Data
public class AdminProperties {
    private String email;
    private String password;
}
