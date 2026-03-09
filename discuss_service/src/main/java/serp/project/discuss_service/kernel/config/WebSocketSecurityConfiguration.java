/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Security filter chain for WebSocket endpoints
 */

package serp.project.discuss_service.kernel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSocketSecurityConfiguration {

    @Bean
    @Order(3)
    public SecurityFilterChain websocketFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.securityMatcher("/ws/**")
                .authorizeHttpRequests(request -> request.anyRequest().permitAll());
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }
}
