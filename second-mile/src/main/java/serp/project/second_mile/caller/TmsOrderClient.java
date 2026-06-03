/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderLookupRequest;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.AuthUtils;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class TmsOrderClient {

    private final RestClient restClient;
    private final AuthUtils authUtils;
    private final String serviceBearerToken;

    @Value("${tms-order.service.lookup-path:/api/v1/internal/orders/lookup}")
    private String lookupPath;

    @Value("${tms-order.service.status-transitions-path:/api/v1/internal/orders/status-transitions}")
    private String statusTransitionsPath;

    public TmsOrderClient(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${tms-order.service.base-url:http://localhost:8099}") String tmsOrderBaseUrl,
            @Value("${tms-order.service.bearer-token:}") String serviceBearerToken
    ) {
        this.authUtils = authUtils;
        this.serviceBearerToken = normalizeText(serviceBearerToken);
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
                .or(() -> serviceBearerToken == null ? java.util.Optional.empty() : java.util.Optional.of(serviceBearerToken))
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
                return new AppException(ErrorCode.BAG_ORDER_NOT_FOUND);
            }
            if (statusCode == 400 || statusCode == 409) {
                return new AppException(ErrorCode.INVALID_REQUEST, "TMS order service rejected order transition.");
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

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
