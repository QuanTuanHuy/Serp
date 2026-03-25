/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String lang = request.getParameter("lang");
                if (StringUtils.hasText(lang)) {
                    Locale locale = Locale.forLanguageTag(lang);
                    for (Locale supportedLocale : getSupportedLocales()) {
                        if (supportedLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
                            return supportedLocale;
                        }
                    }
                }
                return super.resolveLocale(request);
            }
        };

        resolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        resolver.setSupportedLocales(List.of(
                Locale.forLanguageTag("vi"),
                Locale.forLanguageTag("en")
        ));

        return resolver;
    }
}
