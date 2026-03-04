/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.kernel.property.HttpClientProperties;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpClientHelper {

    private final RestClient restClient;
    private final HttpClientProperties httpClientProperties;

    public <T> T get(String uri, Class<T> responseType) {
        return get(uri, null, null, responseType);
    }

    public <T> T get(String uri, MultiValueMap<String, String> queryParams, Class<T> responseType) {
        return get(uri, queryParams, null, responseType);
    }

    public <T> T get(String uri, MultiValueMap<String, String> queryParams, Map<String, String> headers,
            Class<T> responseType) {
        return execute(HttpMethod.GET, uri, queryParams, headers, null, responseType, RetryPolicy.IDEMPOTENT);
    }

    public <T, R> R post(String uri, T requestBody, Class<R> responseType) {
        return post(uri, requestBody, null, responseType);
    }

    public <T, R> R post(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        return execute(HttpMethod.POST, uri, null, headers, requestBody, responseType, RetryPolicy.NONE);
    }

    public <T, R> R postWithRetry(String uri, T requestBody, Class<R> responseType) {
        return postWithRetry(uri, requestBody, null, responseType);
    }

    public <T, R> R postWithRetry(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        return execute(HttpMethod.POST, uri, null, headers, requestBody, responseType,
                RetryPolicy.NON_IDEMPOTENT_ALLOWED);
    }

    public <T, R> R put(String uri, T requestBody, Class<R> responseType) {
        return put(uri, requestBody, null, responseType);
    }

    public <T, R> R put(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        return execute(HttpMethod.PUT, uri, null, headers, requestBody, responseType, RetryPolicy.IDEMPOTENT);
    }

    public <T, R> R patch(String uri, T requestBody, Class<R> responseType) {
        return patch(uri, requestBody, null, responseType);
    }

    public <T, R> R patch(String uri, T requestBody, Map<String, String> headers, Class<R> responseType) {
        return execute(HttpMethod.PATCH, uri, null, headers, requestBody, responseType, RetryPolicy.NONE);
    }

    public <T> T delete(String uri, Class<T> responseType) {
        return delete(uri, null, responseType);
    }

    public <T> T delete(String uri, Map<String, String> headers, Class<T> responseType) {
        return execute(HttpMethod.DELETE, uri, null, headers, null, responseType, RetryPolicy.IDEMPOTENT);
    }

    public MultiValueMap<String, String> buildQueryParams(Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        params.forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null && StringUtils.hasText(value.toString())) {
                builder.queryParam(key, value);
            }
        });
        return builder.build().getQueryParams();
    }

    private <T, R> R execute(HttpMethod method,
            String uri,
            MultiValueMap<String, String> queryParams,
            Map<String, String> headers,
            T requestBody,
            Class<R> responseType,
            RetryPolicy retryPolicy) {
        int maxAttempts = resolveMaxAttempts(retryPolicy);
        int attempt = 1;

        while (true) {
            try {
                R response = sendRequest(method, uri, queryParams, headers, requestBody, responseType);
                if (attempt > 1) {
                    log.info("HTTP {} {} succeeded on attempt {}/{}", method, uri, attempt, maxAttempts);
                }
                return response;
            } catch (RuntimeException exception) {
                AppException mappedException = mapToAppException(exception, uri);
                if (!shouldRetry(method, retryPolicy, mappedException, exception, attempt, maxAttempts)) {
                    logFailure(method, uri, attempt, mappedException);
                    throw mappedException;
                }

                Duration backoffDelay = calculateBackoffDelay(attempt);
                log.warn("HTTP {} {} failed on attempt {}/{} with code {}. Retrying in {} ms",
                        method,
                        uri,
                        attempt,
                        maxAttempts,
                        mappedException.getCode(),
                        backoffDelay.toMillis());

                sleep(backoffDelay);
                attempt++;
            }
        }
    }

    private <T, R> R sendRequest(HttpMethod method,
            String uri,
            MultiValueMap<String, String> queryParams,
            Map<String, String> headers,
            T requestBody,
            Class<R> responseType) {
        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(buildUri(uri, queryParams))
                .headers(httpHeaders -> applyHeaders(httpHeaders, headers));

        if (requestBody != null) {
            if (requestBody instanceof MultiValueMap<?, ?>) {
                requestSpec.contentType(MediaType.APPLICATION_FORM_URLENCODED);
            }
            requestSpec.body(requestBody);
        }

        return requestSpec
                .retrieve()
                .body(responseType);
    }

    private URI buildUri(String uri, MultiValueMap<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.queryParams(queryParams);
        }
        return builder.build(true).toUri();
    }

    private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }

        headers.forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null) {
                httpHeaders.add(key, value);
            }
        });
    }

    private int resolveMaxAttempts(RetryPolicy retryPolicy) {
        return switch (retryPolicy) {
            case IDEMPOTENT -> Math.max(1, httpClientProperties.getIdempotentMaxAttempts());
            case NON_IDEMPOTENT_ALLOWED -> Math.max(1, httpClientProperties.getNonIdempotentRetryMaxAttempts());
            case NONE -> 1;
        };
    }

    private boolean shouldRetry(HttpMethod method,
            RetryPolicy retryPolicy,
            AppException mappedException,
            RuntimeException originalException,
            int attempt,
            int maxAttempts) {
        if (attempt >= maxAttempts) {
            return false;
        }

        if (!isRetryAllowedForMethod(method, retryPolicy)) {
            return false;
        }

        if (isRetryableStatus(mappedException.getCode())) {
            return true;
        }

        return isRetryableTransportException(originalException);
    }

    private boolean isRetryAllowedForMethod(HttpMethod method, RetryPolicy retryPolicy) {
        if (retryPolicy == RetryPolicy.NONE) {
            return false;
        }

        if (retryPolicy == RetryPolicy.NON_IDEMPOTENT_ALLOWED) {
            return true;
        }

        return HttpMethod.GET.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.DELETE.equals(method)
                || HttpMethod.HEAD.equals(method)
                || HttpMethod.OPTIONS.equals(method);
    }

    private boolean isRetryableStatus(int httpStatusCode) {
        return httpStatusCode == 408 || httpStatusCode == 429 || httpStatusCode >= 500;
    }

    private boolean isRetryableTransportException(Throwable throwable) {
        return throwable instanceof ResourceAccessException
                || hasCause(throwable, IOException.class)
                || hasCause(throwable, ConnectException.class)
                || hasCause(throwable, UnknownHostException.class)
                || isTimeoutException(throwable);
    }

    private boolean isTimeoutException(Throwable throwable) {
        return hasCause(throwable, TimeoutException.class)
                || hasCause(throwable, SocketTimeoutException.class)
                || hasCause(throwable, HttpTimeoutException.class);
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Duration calculateBackoffDelay(int attempt) {
        long initialBackoffMs = Math.max(0, httpClientProperties.getInitialBackoffMs());
        long maxBackoffMs = Math.max(initialBackoffMs, httpClientProperties.getMaxBackoffMs());
        double multiplier = httpClientProperties.getBackoffMultiplier() <= 1.0
                ? 2.0
                : httpClientProperties.getBackoffMultiplier();

        long exponentialDelay = (long) (initialBackoffMs * Math.pow(multiplier, Math.max(0, attempt - 1)));
        long cappedDelay = Math.min(exponentialDelay, maxBackoffMs);

        double jitterFactor = Math.max(0.0, Math.min(1.0, httpClientProperties.getJitterFactor()));
        long jitterRange = (long) (cappedDelay * jitterFactor);
        long jitter = jitterRange == 0
                ? 0
                : ThreadLocalRandom.current().nextLong(-jitterRange, jitterRange + 1);

        return Duration.ofMillis(Math.max(0, cappedDelay + jitter));
    }

    private void sleep(Duration backoffDelay) {
        try {
            Thread.sleep(backoffDelay.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Retry interrupted while waiting for downstream service");
        }
    }

    private AppException mapToAppException(RuntimeException exception, String uri) {
        if (exception instanceof AppException appException) {
            return appException;
        }

        if (exception instanceof IllegalArgumentException) {
            return new AppException(ErrorCode.BAD_REQUEST, "Invalid request URI: " + uri);
        }

        if (isTimeoutException(exception)) {
            return new AppException(ErrorCode.REQUEST_TIMEOUT, "Request timeout when calling: " + uri);
        }

        if (exception instanceof ResourceAccessException || exception instanceof RestClientException) {
            return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Cannot reach downstream service: " + uri);
        }

        log.error("Unexpected HTTP client error for URI {}", uri, exception);
        return new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected HTTP client error");
    }

    private void logFailure(HttpMethod method, String uri, int attempt, AppException appException) {
        if (appException.getCode() == 408) {
            log.error("HTTP {} {} timeout after {} attempt(s)", method, uri, attempt);
            return;
        }

        log.error("HTTP {} {} failed after {} attempt(s). Code={}, Message={}",
                method,
                uri,
                attempt,
                appException.getCode(),
                appException.getMessage());
    }

    private enum RetryPolicy {
        NONE,
        IDEMPOTENT,
        NON_IDEMPOTENT_ALLOWED
    }
}
