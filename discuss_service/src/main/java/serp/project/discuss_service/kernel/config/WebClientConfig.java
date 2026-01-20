/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.kernel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import serp.project.discuss_service.core.domain.constant.Constants;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.kernel.utils.DataUtils;

import java.net.URI;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClientWithProxy()))
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    private HttpClient httpClientWithProxy() {
        String proxyEnv = System.getenv("HTTPS_PROXY");
        if (DataUtils.isNullOrEmpty(proxyEnv)) {
            proxyEnv = System.getenv("HTTP_PROXY");
        }

        if (DataUtils.isNullOrEmpty(proxyEnv)) {
            return HttpClient.create();
        }

        try {
            URI proxyUri = URI.create(proxyEnv);
            String host = proxyUri.getHost();
            int port = proxyUri.getPort();

            if (DataUtils.isNullOrEmpty(host) || port == -1) {
                return HttpClient.create();
            }

            return HttpClient.create()
                    .proxy(spec -> spec.type(ProxyProvider.Proxy.HTTP).host(host).port(port));
        } catch (IllegalArgumentException ex) {
            return HttpClient.create();
        }
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
            case 400 -> new AppException(ErrorCode.BAD_REQUEST, message);
            case 401 -> new AppException(ErrorCode.UNAUTHORIZED, message);
            case 403 -> new AppException(ErrorCode.FORBIDDEN, message);
            case 404 -> new AppException(ErrorCode.NOT_FOUND, message);
            case 409 -> new AppException(ErrorCode.CONFLICT, message);
            case 429 -> new AppException(ErrorCode.TOO_MANY_REQUESTS, message);
            case 500 -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, message);
            case 503 -> new AppException(ErrorCode.SERVICE_UNAVAILABLE, message);
            default -> {
                if (statusCode.is5xxServerError()) {
                    yield new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                            message.isEmpty() ? "Server error occurred" : message);
                } else {
                    yield new AppException(ErrorCode.BAD_REQUEST,
                            message.isEmpty() ? "Client error occurred" : message);
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
