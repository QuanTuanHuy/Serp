/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderLookupRequest;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionResponse;
import serp.project.first_mile.caller.dto.tms_order.TmsPickupCandidateRequest;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class TmsOrderClient {

    private final RestClient restClient;
    private final AuthUtils authUtils;

    @Value("${tms-order.service.lookup-path:/api/v1/internal/orders/lookup}")
    private String lookupPath;

    @Value("${tms-order.service.pickup-candidates-path:/api/v1/internal/orders/pickup-candidates}")
    private String pickupCandidatesPath;

    @Value("${tms-order.service.status-transitions-path:/api/v1/internal/orders/status-transitions}")
    private String statusTransitionsPath;

    public TmsOrderClient(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${tms-order.service.base-url:http://localhost:8099}") String tmsOrderBaseUrl
    ) {
        this.authUtils = authUtils;
        this.restClient = restClientBuilder.baseUrl(tmsOrderBaseUrl).build();
    }

    public List<TmsOrderOperationView> lookupByIds(Collection<Long> orderIds) {
        return lookup(TmsOrderLookupRequest.builder()
                .orderIds(orderIds == null ? List.of() : orderIds.stream().toList())
                .build());
    }

    public List<TmsOrderOperationView> lookupByCodes(Collection<String> orderCodes) {
        return lookup(TmsOrderLookupRequest.builder()
                .orderCodes(orderCodes == null ? List.of() : orderCodes.stream().toList())
                .build());
    }

    public List<TmsOrderOperationView> findPickupCandidates(
            String postOfficeCode,
            Collection<OrderStatus> statuses,
            LocalDateTime horizonStart,
            LocalDateTime horizonEnd,
            Integer limit
    ) {
        TmsPickupCandidateRequest request = TmsPickupCandidateRequest.builder()
                .postOfficeCode(postOfficeCode)
                .statuses(statuses == null ? List.of() : statuses.stream().toList())
                .horizonStart(horizonStart)
                .horizonEnd(horizonEnd)
                .limit(limit)
                .build();
        List<TmsOrderOperationView> response = post(
                pickupCandidatesPath,
                request,
                new ParameterizedTypeReference<List<TmsOrderOperationView>>() {
                }
        );
        return response == null ? List.of() : response;
    }

    public TmsOrderStatusTransitionResponse applyTransitions(TmsOrderStatusTransitionRequest request) {
        return post(
                statusTransitionsPath,
                request,
                new ParameterizedTypeReference<TmsOrderStatusTransitionResponse>() {
                }
        );
    }

    private List<TmsOrderOperationView> lookup(TmsOrderLookupRequest request) {
        List<TmsOrderOperationView> response = post(
                lookupPath,
                request,
                new ParameterizedTypeReference<List<TmsOrderOperationView>>() {
                }
        );
        return response == null ? List.of() : response;
    }

    private <T> T post(String path, Object request, ParameterizedTypeReference<T> responseType) {
        try {
            return restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(resolveBearerToken()))
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException exception) {
            log.error("Failed to call tms-order endpoint path={}: {}", path, exception.getMessage(), exception);
            throw toTmsOrderException(exception);
        }
    }

    private String resolveBearerToken() {
        return authUtils.getBearerToken()
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNAUTHORIZED,
                        "Missing authentication token for tms-order service."
                ));
    }

    private AppException toTmsOrderException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(ErrorCode.UNAUTHORIZED, "TMS order service rejected access.");
            }
            if (statusCode == 404) {
                return new AppException(ErrorCode.ORDER_NOT_FOUND);
            }
            if (statusCode == 400 || statusCode == 409) {
                return new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
            return new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "TMS order service returned HTTP " + statusCode + "."
            );
        }
        return new AppException(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Cannot connect to tms-order service. Ensure tms-order is running and TMS_ORDER_SERVICE_BASE_URL is correct."
        );
    }
}
