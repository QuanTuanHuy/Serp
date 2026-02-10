/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.ErrorCode;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("WebClient Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("WebClient Response: {} {}", clientResponse.statusCode(), clientResponse.request().getURI());
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("Unknown error")
                        .flatMap(errorBody -> {
                            log.error("WebClient Error - Status: {}, Body: {}, URL: {}",
                                    clientResponse.statusCode(), errorBody, clientResponse.request().getURI());
                            return Mono.error(createAppException(clientResponse.statusCode(), errorBody));
                        });
            }
            return Mono.just(clientResponse);
        });
    }

    private AppException createAppException(HttpStatusCode statusCode, String errorBody) {
        String message = parseErrorMessage(errorBody);

        return switch (statusCode.value()) {
            case 400 -> new AppException(ErrorCode.BAD_REQUEST,
                    message.isEmpty() ? ErrorCode.BAD_REQUEST.getMessage() : message);
            case 401 -> new AppException(ErrorCode.UNAUTHORIZED,
                    message.isEmpty() ? ErrorCode.UNAUTHORIZED.getMessage() : message);
            case 403 -> new AppException(ErrorCode.FORBIDDEN,
                    message.isEmpty() ? ErrorCode.FORBIDDEN.getMessage() : message);
            case 404 -> new AppException(ErrorCode.NOT_FOUND,
                    message.isEmpty() ? ErrorCode.NOT_FOUND.getMessage() : message);
            case 409 -> new AppException(ErrorCode.CONFLICT,
                    message.isEmpty() ? ErrorCode.CONFLICT.getMessage() : message);
            case 429 -> new AppException(ErrorCode.TOO_MANY_REQUESTS,
                    message.isEmpty() ? ErrorCode.TOO_MANY_REQUESTS.getMessage() : message);
            default -> {
                if (statusCode.is5xxServerError()) {
                    yield new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                            message.isEmpty() ? ErrorCode.INTERNAL_SERVER_ERROR.getMessage() : message);
                } else {
                    yield new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                            message.isEmpty() ? "Unknown error" : message);
                }
            }
        };
    }

    private String parseErrorMessage(String errorBody) {
        if (errorBody == null || errorBody.trim().isEmpty()) {
            return "";
        }

        try {
            JsonNode rootNode = objectMapper.readTree(errorBody);

            if (rootNode.has("message")) {
                String extracted = rootNode.get("message").asText();
                if (!extracted.isEmpty())
                    return extracted;
            }
            if (rootNode.has("error_description")) {
                String extracted = rootNode.get("error_description").asText();
                if (!extracted.isEmpty())
                    return extracted;
            }
            if (rootNode.has("error")) {
                String extracted = rootNode.get("error").asText();
                if (!extracted.isEmpty())
                    return extracted;
            }
            if (rootNode.has("errors") && rootNode.get("errors").isArray()) {
                JsonNode errorsNode = rootNode.get("errors");
                if (!errorsNode.isEmpty()) {
                    String extracted = errorsNode.get(0).asText();
                    if (!extracted.isEmpty())
                        return extracted;
                }
            }
            if (rootNode.has("detail")) {
                String extracted = rootNode.get("detail").asText();
                if (!extracted.isEmpty())
                    return extracted;
            }
            if (rootNode.has("data")) {
                String extracted = rootNode.get("data").asText();
                if (!extracted.isEmpty())
                    return extracted;
            }

            return errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;

        } catch (Exception e) {
            log.warn("Failed to parse error message from response body: {}", errorBody);
            return errorBody.length() > 100 ? errorBody.substring(0, 100) + "..." : errorBody;
        }
    }
}
