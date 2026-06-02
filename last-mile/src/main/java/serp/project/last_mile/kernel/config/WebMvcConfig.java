/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.kernel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import serp.project.last_mile.kernel.interceptor.RestEndpointLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final RestEndpointLoggingInterceptor restEndpointLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restEndpointLoggingInterceptor)
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
