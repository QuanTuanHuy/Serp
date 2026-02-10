/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.kernel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import serp.project.mailservice.kernel.property.SendGridProperties;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class SendGridConfig {
    private final SendGridProperties sendGridProperties;

    @Bean
    public WebClient sendGridWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(sendGridProperties.getTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(sendGridProperties.getApiUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sendGridProperties.getApiKey())
                .build();
    }
}
