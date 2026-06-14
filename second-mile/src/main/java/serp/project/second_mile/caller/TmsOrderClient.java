/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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
    private final String internalApiKey;

    @Value("${tms-order.service.lookup-path:/api/v1/internal/orders/lookup}")
    private String lookupPath;

    @Value("${tms-order.service.status-transitions-path:/api/v1/internal/orders/status-transitions}")
    private String statusTransitionsPath;

    public TmsOrderClient(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${tms-order.service.base-url:http://localhost:8105}") String tmsOrderBaseUrl,
            @Value("${internal-api.api-key:}") String internalApiKey
    ) {
        this.authUtils = authUtils;
        this.internalApiKey = normalizeText(internalApiKey);
        this.restClient = restClientBuilder.baseUrl(tmsOrderBaseUrl).build();
    }

    public List<TmsOrderOperationView> lookupByIds(Collection<Long> orderIds) {
        return lookupByIds(null, orderIds);
    }

    public List<TmsOrderOperationView> lookupByIds(Long tenantId, Collection<Long> orderIds) {
        return lookup(TmsOrderLookupRequest.builder()
                .orderIds(orderIds == null ? List.of() : orderIds.stream().toList())
                .build(), tenantId);
    }

    public List<TmsOrderOperationView> lookupByCodes(Collection<String> orderCodes) {
        return lookupByCodes(null, orderCodes);
    }

    public List<TmsOrderOperationView> lookupByCodes(Long tenantId, Collection<String> orderCodes) {
        return lookup(TmsOrderLookupRequest.builder()
                .orderCodes(orderCodes == null ? List.of() : orderCodes.stream().toList())
                .build(), tenantId);
    }

    public TmsOrderStatusTransitionResponse applyTransitions(TmsOrderStatusTransitionRequest request) {
        return applyTransitions(request, null);
    }

    public TmsOrderStatusTransitionResponse applyTransitions(TmsOrderStatusTransitionRequest request, Long tenantId) {
        return post(
                statusTransitionsPath,
                request,
                tenantId,
                new ParameterizedTypeReference<TmsOrderStatusTransitionResponse>() {
                }
        );
    }

    private List<TmsOrderOperationView> lookup(TmsOrderLookupRequest request, Long tenantId) {
        List<TmsOrderOperationView> response = post(
                lookupPath,
                request,
                tenantId,
                new ParameterizedTypeReference<List<TmsOrderOperationView>>() {
                }
        );
        return response == null ? List.of() : response;
    }

    private <T> T post(String path, Object request, Long tenantId, ParameterizedTypeReference<T> responseType) {
        try {
            return restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyAuthHeaders(headers, tenantId))
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException exception) {
            log.error("Failed to call tms-order endpoint path={}: {}", path, exception.getMessage(), exception);
            throw toTmsOrderException(exception);
        }
    }

    private void applyAuthHeaders(HttpHeaders headers, Long tenantId) {
        Long resolvedTenantId = tenantId == null
                ? authUtils.getCurrentTenantId().orElse(null)
                : tenantId;
        if (internalApiKey == null || resolvedTenantId == null) {
            throw new AppException(
                    ErrorCode.UNAUTHORIZED,
                    "Missing internal API key or tenant id for tms-order service call."
            );
        }
        headers.set("X-Internal-Api-Key", internalApiKey);
        headers.set("X-Tenant-Id", resolvedTenantId.toString());
        headers.set("X-Internal-Service", "second-mile");
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
