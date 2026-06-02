/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.last_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.last_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.last_mile.caller.dto.payment.PaymentQueryOrderRequest;
import serp.project.last_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.last_mile.exception.AppException;
import serp.project.last_mile.exception.ErrorCode;
import serp.project.last_mile.kernel.utils.AuthUtils;

@Component
@Slf4j
public class PaymentServiceCaller {

    private final RestClient restClient;
    private final AuthUtils authUtils;

    @Value("${payment.service.gateway-code:zalopay}")
    private String gatewayCode;

    @Value("${payment.service.create-order-path-template:/v1/payments/%s/create-order}")
    private String createOrderPath;

    @Value("${payment.service.query-order-path-template:/v1/payments/%s/query-order}")
    private String queryOrderPath;

    public PaymentServiceCaller(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${payment.service.base-url:http://localhost:8096}") String paymentServiceBaseUrl
    ) {
        this.authUtils = authUtils;
        this.restClient = restClientBuilder.baseUrl(paymentServiceBaseUrl).build();
    }

    public PaymentCreateOrderResponse createOrder(PaymentCreateOrderRequest request) {
        try {
            PaymentCreateOrderResponse response = restClient.post()
                    .uri(resolveCreateOrderPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PaymentCreateOrderResponse.class);
            if (response == null) {
                throw new AppException(
                        ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "Payment service returned empty create-order response."
                );
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Failed to call payment create-order endpoint: {}", exception.getMessage(), exception);
            throw toPaymentServiceException(exception);
        }
    }

    public PaymentQueryOrderResponse queryOrderStatus(String appTransId) {
        String bearerToken = authUtils.getBearerToken()
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNAUTHORIZED,
                        "Missing authentication token for payment verification."
                ));

        try {
            PaymentQueryOrderResponse response = restClient.post()
                    .uri(resolveQueryOrderPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(PaymentQueryOrderRequest.builder().appTransId(appTransId).build())
                    .retrieve()
                    .body(PaymentQueryOrderResponse.class);
            if (response == null) {
                throw new AppException(
                        ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "Payment service returned empty query-order response."
                );
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Failed to call payment query-order endpoint: {}", exception.getMessage(), exception);
            throw toPaymentServiceException(exception);
        }
    }

    private AppException toPaymentServiceException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(
                        ErrorCode.UNAUTHORIZED,
                        "Payment service rejected the request. Please sign in again and retry."
                );
            }
            return new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Payment service returned HTTP " + statusCode + "."
            );
        }
        return new AppException(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Cannot connect to payment service. Ensure payment-service is running and PAYMENT_SERVICE_BASE_URL is correct."
        );
    }

    private String resolveCreateOrderPath() {
        return String.format(createOrderPath, sanitizeGatewayCode());
    }

    private String resolveQueryOrderPath() {
        return String.format(queryOrderPath, sanitizeGatewayCode());
    }

    private String sanitizeGatewayCode() {
        if (gatewayCode == null || gatewayCode.isBlank()) {
            return "zalopay";
        }
        return gatewayCode.trim().toLowerCase();
    }
}
