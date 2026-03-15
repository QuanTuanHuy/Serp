/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.kernel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;
import serp.project.mailservice.kernel.property.JavaMailProperties;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.java-mail.enabled", havingValue = "true")
public class JavaMailConfig {

    private final JavaMailProperties javaMailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        validateJavaMailConfiguration();

        mailSender.setHost(javaMailProperties.getHost());

        if (javaMailProperties.getPort() != null) {
            mailSender.setPort(javaMailProperties.getPort());
        }

        if (StringUtils.hasText(javaMailProperties.getUsername())) {
            mailSender.setUsername(javaMailProperties.getUsername());
        }

        if (StringUtils.hasText(javaMailProperties.getPassword())) {
            mailSender.setPassword(javaMailProperties.getPassword());
        }

        String protocol = StringUtils.hasText(javaMailProperties.getProtocol())
                ? javaMailProperties.getProtocol()
                : "smtp";
        mailSender.setProtocol(protocol);

        String defaultEncoding = StringUtils.hasText(javaMailProperties.getDefaultEncoding())
                ? javaMailProperties.getDefaultEncoding()
                : "UTF-8";
        mailSender.setDefaultEncoding(defaultEncoding);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(javaMailProperties.getAuth())));
        props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(javaMailProperties.getStarttlsEnable())));
        props.put("mail.smtp.starttls.required", String.valueOf(Boolean.TRUE.equals(javaMailProperties.getStarttlsRequired())));
        props.put("mail.smtp.ssl.enable", String.valueOf(Boolean.TRUE.equals(javaMailProperties.getSslEnable())));

        int timeoutMillis = Math.max(1, javaMailProperties.getTimeoutSeconds() != null
                ? javaMailProperties.getTimeoutSeconds()
                : 10) * 1000;
        props.put("mail.smtp.connectiontimeout", String.valueOf(timeoutMillis));
        props.put("mail.smtp.timeout", String.valueOf(timeoutMillis));
        props.put("mail.smtp.writetimeout", String.valueOf(timeoutMillis));

        log.info("JavaMail sender configured: host={}, port={}, protocol={}, auth={}, starttls={}, ssl={}",
                mailSender.getHost(),
                mailSender.getPort(),
                protocol,
                Boolean.TRUE.equals(javaMailProperties.getAuth()),
                Boolean.TRUE.equals(javaMailProperties.getStarttlsEnable()),
                Boolean.TRUE.equals(javaMailProperties.getSslEnable()));

        return mailSender;
    }

    private void validateJavaMailConfiguration() {
        if (!StringUtils.hasText(javaMailProperties.getHost())) {
            throw new IllegalStateException("JavaMail host is required when JAVA_MAIL provider is enabled");
        }

        if (javaMailProperties.getPort() == null || javaMailProperties.getPort() <= 0) {
            throw new IllegalStateException("JavaMail port must be greater than 0");
        }

        boolean authEnabled = Boolean.TRUE.equals(javaMailProperties.getAuth());
        if (authEnabled && !StringUtils.hasText(javaMailProperties.getUsername())) {
            throw new IllegalStateException("JavaMail auth is enabled but username is empty (set MAIL_USERNAME)");
        }

        if (authEnabled && !StringUtils.hasText(javaMailProperties.getPassword())) {
            throw new IllegalStateException("JavaMail auth is enabled but password is empty (set MAIL_PASSWORD)");
        }

        if (Boolean.TRUE.equals(javaMailProperties.getStarttlsEnable())
                && Boolean.TRUE.equals(javaMailProperties.getSslEnable())) {
            log.warn("Both STARTTLS and SSL are enabled for JavaMail. Verify SMTP security mode configuration");
        }
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.java-mail.test-connection-on-startup", havingValue = "true")
    public JavaMailConnectionVerifier javaMailConnectionVerifier(JavaMailSender javaMailSender) {
        return new JavaMailConnectionVerifier(javaMailSender);
    }

    @Slf4j
    private static class JavaMailConnectionVerifier {
        JavaMailConnectionVerifier(JavaMailSender javaMailSender) {
            try {
                if (javaMailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                    javaMailSenderImpl.testConnection();
                } else {
                    javaMailSender.createMimeMessage();
                    log.warn("JavaMail startup check fallback: sender type does not support testConnection");
                }
                log.info("JavaMail startup connection check passed");
            } catch (Exception ex) {
                log.error("JavaMail startup connection check failed: {}", ex.getMessage());
                throw new IllegalStateException("JavaMail startup connection check failed", ex);
            }
        }
    }
}
