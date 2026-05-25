package com.example.ttcrs.kernel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpClientHelper {

    private final WebClient webClient;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public <T> Mono<T> get(String uri, MultiValueMap<String, String> queryParams,
                           Map<String, String> headers, Class<T> responseType) {
        log.debug("GET {}", uri);
        return webClient.get()
                .uri(buildUri(uri, queryParams))
                .headers(h -> { if (headers != null) headers.forEach(h::add); })
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryable)
                        .onRetryExhaustedThrow((spec, signal) -> {
                            log.error("Retry exhausted for {}", uri);
                            return new RuntimeException("Service unavailable after retries: " + uri);
                        }))
                .doOnError(e -> log.error("GET {} failed: {}", uri, e.getMessage()));
    }

    public <T, R> Mono<R> post(String uri, T body, Class<R> responseType) {
        log.debug("POST {}", uri);
        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryable)
                        .onRetryExhaustedThrow((spec, signal) ->
                                new RuntimeException("Service unavailable after retries: " + uri)))
                .doOnError(e -> log.error("POST {} failed: {}", uri, e.getMessage()));
    }

    private URI buildUri(String uri, MultiValueMap<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        if (queryParams != null) builder.queryParams(queryParams);
        return builder.build().toUri();
    }

    private boolean isRetryable(Throwable t) {
        return t instanceof TimeoutException || t instanceof java.io.IOException;
    }
}
