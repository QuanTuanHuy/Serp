/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;

@Component
@Slf4j
public class PaymentServiceCaller {

    private final RestClient restClient;

    @Value("${payment.service.gateway-code:zalopay}")
    private String gatewayCode;

    @Value("${payment.service.create-order-path-template:/v1/payments/%s/create-order}")
    private String createOrderPath;

    @Value("${payment.service.query-order-path-template:/v1/payments/%s/query-order}")
    private String queryOrderPath;

    public PaymentServiceCaller(
            RestClient.Builder restClientBuilder,
            @Value("${payment.service.base-url:http://localhost:8096}") String paymentServiceBaseUrl) {
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
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Payment service returned empty create-order response.");
            }
            return response;
        } catch (RestClientException ex) {
            log.error("Failed to call payment create-order endpoint: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Cannot connect to payment service.");
        }
    }

    public PaymentQueryOrderResponse queryOrderStatus(String appTransId) {
        try {
            PaymentQueryOrderResponse response = restClient.post()
                    .uri(resolveQueryOrderPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(PaymentQueryOrderRequest.builder().appTransId(appTransId).build())
                    .retrieve()
                    .body(PaymentQueryOrderResponse.class);
            if (response == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Payment service returned empty query-order response.");
            }
            return response;
        } catch (RestClientException ex) {
            log.error("Failed to call payment query-order endpoint: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Cannot connect to payment service.");
        }
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
