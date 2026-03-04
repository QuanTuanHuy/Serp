/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.kernel.property.HttpClientProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestClientConfig {

    private final ObjectMapper objectMapper;
    private final HttpClientProperties httpClientProperties;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(restClientRequestFactory())
                .requestInterceptor(loggingInterceptor())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String errorBody = extractErrorBody(response);
                    log.error("RestClient Error - Method: {}, Status: {}, URL: {}, Body: {}",
                            request.getMethod(), response.getStatusCode(), request.getURI(), errorBody);
                    throw createAppException(response.getStatusCode(), errorBody);
                })
                .build();
    }

    private ClientHttpRequestFactory restClientRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(httpClientProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(httpClientProperties.getReadTimeoutMs());

        Proxy proxy = resolveProxy();
        if (proxy != null) {
            requestFactory.setProxy(proxy);
        }
        return requestFactory;
    }

    private Proxy resolveProxy() {
        String proxyEnv = resolveProxyEnv();
        if (!StringUtils.hasText(proxyEnv)) {
            return null;
        }

        try {
            URI proxyUri = URI.create(proxyEnv.trim());
            String host = proxyUri.getHost();
            int port = resolveProxyPort(proxyUri);

            if (!StringUtils.hasText(host) || port <= 0) {
                log.warn("Ignoring invalid proxy URL from environment: {}", proxyEnv);
                return null;
            }

            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to parse proxy URL from environment: {}", proxyEnv);
            return null;
        }
    }

    private String resolveProxyEnv() {
        String httpsProxy = System.getenv("HTTPS_PROXY");
        if (StringUtils.hasText(httpsProxy)) {
            return httpsProxy;
        }

        String httpProxy = System.getenv("HTTP_PROXY");
        if (StringUtils.hasText(httpProxy)) {
            return httpProxy;
        }

        return null;
    }

    private int resolveProxyPort(URI proxyUri) {
        if (proxyUri.getPort() > 0) {
            return proxyUri.getPort();
        }
        if ("https".equalsIgnoreCase(proxyUri.getScheme())) {
            return 443;
        }
        return 80;
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            long start = System.currentTimeMillis();
            log.debug("RestClient Request: {} {}", request.getMethod(), request.getURI());

            ClientHttpResponse response = execution.execute(request, body);
            long elapsedMs = System.currentTimeMillis() - start;
            log.debug("RestClient Response: {} {} ({} ms)",
                    response.getStatusCode(), request.getURI(), elapsedMs);
            return response;
        };
    }

    private String extractErrorBody(ClientHttpResponse response) {
        try {
            String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(errorBody)) {
                return "Unknown error";
            }
            return errorBody;
        } catch (IOException ex) {
            return "Unable to read error response body";
        }
    }

    private AppException createAppException(HttpStatusCode statusCode, String errorBody) {
        String message = parseErrorMessage(errorBody);

        return switch (statusCode.value()) {
            case 400 -> new AppException(ErrorCode.BAD_REQUEST, message);
            case 401 -> new AppException(ErrorCode.UNAUTHORIZED, message);
            case 403 -> new AppException(ErrorCode.FORBIDDEN, message);
            case 404 -> new AppException(ErrorCode.NOT_FOUND, message);
            case 408 -> new AppException(ErrorCode.REQUEST_TIMEOUT, message);
            case 409 -> new AppException(ErrorCode.CONFLICT, message);
            case 429 -> new AppException(ErrorCode.TOO_MANY_REQUESTS, message);
            case 500 -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, message);
            case 502, 503, 504 -> new AppException(ErrorCode.SERVICE_UNAVAILABLE, message);
            default -> {
                if (statusCode.is5xxServerError()) {
                    yield new AppException(ErrorCode.SERVICE_UNAVAILABLE,
                            message.isEmpty() ? "Downstream service is unavailable" : message);
                } else {
                    yield new AppException(ErrorCode.BAD_REQUEST,
                            message.isEmpty() ? "Downstream request failed" : message);
                }
            }
        };
    }

    private String parseErrorMessage(String errorBody) {
        if (!StringUtils.hasText(errorBody)) {
            return "";
        }

        try {
            JsonNode rootNode = objectMapper.readTree(errorBody);

            if (rootNode.has("message")) {
                String extracted = rootNode.get("message").asText();
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }

            if (rootNode.has("detail")) {
                String extracted = rootNode.get("detail").asText();
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }

            if (rootNode.has("error_description")) {
                String extracted = rootNode.get("error_description").asText();
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }

            if (rootNode.has("error")) {
                String extracted = rootNode.get("error").asText();
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }

            return errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
        } catch (Exception e) {
            log.debug("Failed to parse error body as JSON");
            return errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
        }
    }
}
