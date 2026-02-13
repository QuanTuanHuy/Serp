/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.core.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import io.github.serp.platform.core.context.CorrelationIdFilter;
import io.github.serp.platform.core.context.SerpCoreProperties;

@AutoConfiguration
@EnableConfigurationProperties(SerpCoreProperties.class)
public class SerpCoreAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "serp.core", name = "correlation-id-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(SerpCoreProperties properties) {
        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorrelationIdFilter(properties.getCorrelationIdHeader()));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
