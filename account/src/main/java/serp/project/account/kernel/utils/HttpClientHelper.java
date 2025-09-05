/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.exception.AppException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpClientHelper {

    private final WebClient webClient;

    public <T> Mono<T> get(String uri, Class<T> responseType) {
        return get(uri, responseType, 3);
    }

    public <T> Mono<T> get(String uri, Class<T> responseType, int maxRetries) {
        log.info("Making GET request to: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Retry exhausted for GET request to: {}", uri);
                            return new AppException(
                                    "Service temporarily unavailable after retries",
                                    Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
                        }))
                .doOnSuccess(response -> log.info("GET request successful to: {}", uri))
                .doOnError(error -> log.error("GET request failed to: {}", uri));
    }

    public <T, R> Mono<R> post(String uri, T requestBody, Class<R> responseType) {
        return post(uri, requestBody, responseType, 2);
    }

    public <T, R> Mono<R> post(String uri, T requestBody, Class<R> responseType, int maxRetries) {
        log.info("Making POST request to: {}", uri);

        return webClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                        .filter(this::isRetryableError)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Retry exhausted for POST request to: {}", uri);
                            return new AppException(
                                    "Service temporarily unavailable after retries",
                                    Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
                        }))
                .doOnSuccess(response -> log.info("POST request successful to: {}", uri))
                .doOnError(error -> log.error("POST request failed to: {}", uri));
    }

    public <T, R> Mono<R> put(String uri, T requestBody, Class<R> responseType) {
        log.info("Making PUT request to: {}", uri);

        return webClient.put()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                .doOnSuccess(response -> log.info("PUT request successful to: {}", uri))
                .doOnError(error -> log.error("PUT request failed to: {}", uri));
    }

    public <T> Mono<T> delete(String uri, Class<T> responseType) {
        log.info("Making DELETE request to: {}", uri);

        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> log.info("DELETE request successful to: {}", uri))
                .doOnError(error -> log.error("DELETE request failed to: {}", uri));
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof AppException appEx) {
            return appEx.getCode() >= 500;
        }

        return !(throwable instanceof IllegalArgumentException) &&
                !(throwable instanceof IllegalStateException);
    }

    public Mono<String> exampleServiceCall(String endpoint, Object requestData) {
        return post(endpoint, requestData, String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        return new AppException(
                                "Request timeout",
                                Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
                    }
                    return throwable;
                });
    }
}
