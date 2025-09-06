/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.config;

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
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.exception.AppException;

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
            case 400 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.BAD_REQUEST : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            case 401 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.UNAUTHORIZED : message,
                    Constants.HttpStatusCode.UNAUTHORIZED);
            case 403 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.FORBIDDEN : message,
                    Constants.HttpStatusCode.FORBIDDEN);
            case 404 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.NOT_FOUND : message,
                    Constants.HttpStatusCode.NOT_FOUND);
            case 409 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.CONFLICT : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            case 429 -> new AppException(message.isEmpty() ? Constants.ErrorMessage.TOO_MANY_REQUESTS : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            default -> {
                if (statusCode.is5xxServerError()) {
                    yield new AppException(message.isEmpty() ? Constants.ErrorMessage.INTERNAL_SERVER_ERROR : message,
                            Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
                } else {
                    yield new AppException(message.isEmpty() ? Constants.ErrorMessage.UNKNOWN_ERROR : message,
                            Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
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

            return errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;

        } catch (Exception e) {
            log.warn("Failed to parse error message from response body: {}", errorBody);
            return errorBody.length() > 100 ? errorBody.substring(0, 100) + "..." : errorBody;
        }
    }
}
