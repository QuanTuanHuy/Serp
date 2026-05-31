package serp.project.school_bus_service.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

/**
 * Configures Spring's {@link MessageSource} to load i18n message bundles from
 * {@code classpath:i18n/messages*.properties}.
 *
 * <p>Priority: {@code messages_en} → {@code messages} (fallback).
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setDefaultLocale(java.util.Locale.ENGLISH);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}
