/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.kernel.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.kernel.property.HttpClientProperties;

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

import java.io.IOException;
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
        return requestFactory;
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
