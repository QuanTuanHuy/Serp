/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.kernel.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.filter")
public class RequestFilter {
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