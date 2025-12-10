/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.kernel.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.kernel.utils.DataUtils;

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
            case 400 -> new AppException(message.isEmpty() ? ErrorMessage.BAD_REQUEST : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            case 401 -> new AppException(message.isEmpty() ? ErrorMessage.UNAUTHORIZED : message,
                    Constants.HttpStatusCode.UNAUTHORIZED);
            case 403 -> new AppException(message.isEmpty() ? ErrorMessage.FORBIDDEN : message,
                    Constants.HttpStatusCode.FORBIDDEN);
            case 404 -> new AppException(message.isEmpty() ? ErrorMessage.NOT_FOUND : message,
                    Constants.HttpStatusCode.NOT_FOUND);
            case 409 -> new AppException(message.isEmpty() ? ErrorMessage.CONFLICT : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            case 429 -> new AppException(message.isEmpty() ? ErrorMessage.TOO_MANY_REQUESTS : message,
                    Constants.HttpStatusCode.BAD_REQUEST);
            default -> {
                if (statusCode.is5xxServerError()) {
                    yield new AppException(message.isEmpty() ? ErrorMessage.INTERNAL_SERVER_ERROR : message,
                            Constants.HttpStatusCode.INTERNAL_SERVER_ERROR);
                } else {
                    yield new AppException(message.isEmpty() ? ErrorMessage.UNKNOWN_ERROR : message,
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
