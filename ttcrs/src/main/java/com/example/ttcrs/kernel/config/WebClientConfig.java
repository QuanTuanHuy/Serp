package com.example.ttcrs.kernel.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.URI;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClientWithProxy()))
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter())
                .codecs(c -> c.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    private HttpClient httpClientWithProxy() {
        String proxyEnv = System.getenv("HTTPS_PROXY");
        if (proxyEnv == null || proxyEnv.isBlank()) proxyEnv = System.getenv("HTTP_PROXY");
        if (proxyEnv == null || proxyEnv.isBlank()) return HttpClient.create();

        try {
            URI uri = URI.create(proxyEnv);
            String host = uri.getHost();
            int port = uri.getPort();
            if (host == null || host.isBlank() || port == -1) return HttpClient.create();
            return HttpClient.create()
                    .proxy(spec -> spec
                            .type(ProxyProvider.Proxy.HTTP)
                            .host(host)
                            .port(port)
                            .nonProxyHosts("localhost|127.*|[::1]"));
        } catch (IllegalArgumentException ex) {
            return HttpClient.create();
        }
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("WebClient → {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.debug("WebClient ← {} {}", res.statusCode(), res.request().getURI());
            return Mono.just(res);
        });
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            if (res.statusCode().isError()) {
                return res.bodyToMono(String.class)
                        .defaultIfEmpty("Unknown error")
                        .flatMap(body -> {
                            log.error("WebClient error — status={}, url={}, body={}",
                                    res.statusCode(), res.request().getURI(), body);
                            String msg = parseErrorMessage(body);
                            return Mono.error(new RuntimeException(
                                    "External service error [" + res.statusCode().value() + "]: " + msg));
                        });
            }
            return Mono.just(res);
        });
    }

    private String parseErrorMessage(String body) {
        if (body == null || body.isBlank()) return "Unknown error";
        try {
            JsonNode root = objectMapper.readTree(body);
            for (String field : new String[]{"message", "error_description", "error"}) {
                if (root.has(field)) {
                    String val = root.get(field).asText();
                    if (!val.isBlank()) return val;
                }
            }
        } catch (Exception ignored) {}
        return body.length() > 200 ? body.substring(0, 200) + "..." : body;
    }
}
