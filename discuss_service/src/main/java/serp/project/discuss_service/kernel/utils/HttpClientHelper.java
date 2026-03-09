/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.kernel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;

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

    public <T> Mono<T> get(String uri, Class<T> responseType) {
        return get(uri, null, null, responseType);
    }

    public <T> Mono<T> get(String uri, MultiValueMap<String, String> queryParams, Class<T> responseType) {
        return get(uri, queryParams, null, responseType);
    }

    public <T> Mono<T> get(String uri, MultiValueMap<String, String> queryParams, Map<String, String> headers,
            Class<T> responseType) {
        log.info("Making GET request to: {}", uri);

        return webClient.get()
                .uri(uriBuilder -> buildUri(uriBuilder, uri, queryParams))
                .headers(httpHeaders -> {
                    if (headers != null)
                        headers.forEach(httpHeaders::add);
                })
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((spec, signal) -> handleRetryExhausted(uri)))
                .doOnError(error -> handleError(error, uri));
    }

    public <T, R> Mono<R> post(String uri, T requestBody, Class<R> responseType) {
        return post(uri, requestBody, null, responseType);
    }

    public <T, R> Mono<R> post(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        log.info("Making POST request to: {}", uri);

        return webClient.post()
                .uri(uriBuilder -> buildUri(uriBuilder, uri, null))
                .headers(httpHeaders -> {
                    if (headers != null)
                        headers.forEach(httpHeaders::add);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((spec, signal) -> handleRetryExhausted(uri)))
                .doOnSuccess(response -> log.info("POST request successful to: {}", uri))
                .doOnError(error -> handleError(error, uri));
    }

    public <T, R> Mono<R> put(String uri, T requestBody, Class<R> responseType) {
        return put(uri, requestBody, null, responseType);
    }

    public <T, R> Mono<R> put(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        log.info("Making PUT request to: {}", uri);

        return webClient.put()
                .uri(uriBuilder -> buildUri(uriBuilder, uri, null))
                .headers(httpHeaders -> {
                    if (headers != null)
                        headers.forEach(httpHeaders::add);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((spec, signal) -> handleRetryExhausted(uri)))
                .doOnSuccess(response -> log.info("PUT request successful to: {}", uri))
                .doOnError(error -> handleError(error, uri));
    }

    public <T, R> Mono<R> patch(String uri, T requestBody, Class<R> responseType) {
        return patch(uri, requestBody, null, responseType);
    }

    public <T, R> Mono<R> patch(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        log.info("Making PATCH request to: {}", uri);

        return webClient.patch()
                .uri(uriBuilder -> buildUri(uriBuilder, uri, null))
                .headers(httpHeaders -> {
                    if (headers != null)
                        headers.forEach(httpHeaders::add);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((spec, signal) -> handleRetryExhausted(uri)))
                .doOnSuccess(response -> log.info("PATCH request successful to: {}", uri))
                .doOnError(error -> handleError(error, uri));
    }

    public <T> Mono<T> delete(String uri, Class<T> responseType) {
        return delete(uri, null, responseType);
    }

    public <T> Mono<T> delete(String uri, Map<String, String> headers, Class<T> responseType) {
        log.info("Making DELETE request to: {}", uri);

        return webClient.delete()
                .uri(uriBuilder -> buildUri(uriBuilder, uri, null))
                .headers(httpHeaders -> {
                    if (headers != null)
                        headers.forEach(httpHeaders::add);
                })
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> log.info("DELETE request successful to: {}", uri))
                .doOnError(error -> handleError(error, uri));
    }

    public MultiValueMap<String, String> buildQueryParams(Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        params.forEach((key, value) -> {
            if (key != null && !key.isEmpty() && value != null && !value.toString().isEmpty()) {
                builder.queryParam(key, value);
            }
        });
        return builder.build().getQueryParams();
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof AppException appEx) {
            return appEx.getCode() >= 500;
        }

        return throwable instanceof TimeoutException ||
                throwable instanceof java.io.IOException;
    }

    private URI buildUri(UriBuilder uriBuilder, String uri, MultiValueMap<String, String> queryParams) {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
            if (queryParams != null) {
                builder.queryParams(queryParams);
            }
            return builder.build().toUri();
        }
        uriBuilder.path(uri);
        if (queryParams != null) {
            uriBuilder.queryParams(queryParams);
        }
        return uriBuilder.build();
    }

    private Throwable handleRetryExhausted(String uri) {
        log.error("Retry exhausted for request to: {}", uri);
        return new AppException(ErrorCode.SERVICE_UNAVAILABLE);
    }

    private void handleError(Throwable error, String uri) {
        if (error instanceof TimeoutException) {
            log.error("Request timeout for: {}", uri);
        } else {
            log.error("Request failed for: {} with error: {}", uri, error.getMessage());
        }
    }

    public Mono<String> exampleServiceCall(String endpoint, Object requestData) {
        return post(endpoint, requestData, String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        return new AppException(ErrorCode.REQUEST_TIMEOUT);
                    }
                    return throwable;
                });
    }
}
