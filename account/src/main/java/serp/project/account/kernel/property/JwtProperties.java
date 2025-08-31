/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
public class JwtProperties {
    private String privateKey;
    private String publicKey;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
    private String header;
    private String prefix;
}