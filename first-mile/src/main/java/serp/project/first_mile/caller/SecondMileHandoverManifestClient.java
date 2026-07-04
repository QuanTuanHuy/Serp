/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;

@Component
@Slf4j
public class SecondMileHandoverManifestClient {
    private final RestClient restClient;
    private final AuthUtils authUtils;
    private final ObjectMapper objectMapper;
    private final String internalApiKey;

    @Value("${second-mile.service.validate-handover-outbound-sync-path:/api/v1/handover-manifests/internal/validate-outbound-sync}")
    private String validateOutboundSyncPath;

    public SecondMileHandoverManifestClient(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            ObjectMapper objectMapper,
            @Value("${second-mile.service.base-url:http://localhost:8102}") String secondMileBaseUrl,
            @Value("${internal-api.api-key:}") String internalApiKey
    ) {
        this.authUtils = authUtils;
        this.objectMapper = objectMapper;
        this.internalApiKey = normalizeText(internalApiKey);
        this.restClient = restClientBuilder.baseUrl(secondMileBaseUrl).build();
    }

    public void validateOutboundSync(HandoverManifestSyncEvent event) {
        try {
            restClient.post()
                    .uri(validateOutboundSyncPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyAuthHeaders(headers, event == null ? null : event.getTenantId()))
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.error(
                    "Failed to validate handover outbound sync path={} manifestCode={}: {}",
                    validateOutboundSyncPath,
                    event == null ? null : event.getManifestCode(),
                    exception.getMessage(),
                    exception
            );
            throw toSecondMileException(exception);
        }
    }

    private void applyAuthHeaders(HttpHeaders headers, Long tenantId) {
        Long resolvedTenantId = tenantId == null
                ? authUtils.getCurrentTenantId().orElse(null)
                : tenantId;
        if (internalApiKey == null || resolvedTenantId == null) {
            throw new AppException(
                    ErrorCode.UNAUTHORIZED,
                    "Missing internal API key or tenant id for second-mile service call."
            );
        }
        headers.set("X-Internal-Api-Key", internalApiKey);
        headers.set("X-Tenant-Id", resolvedTenantId.toString());
        headers.set("X-Internal-Service", "first-mile");
    }

    private AppException toSecondMileException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            String detail = extractResponseDetail(responseException);
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(ErrorCode.UNAUTHORIZED, nonBlankOrDefault(
                        detail,
                        "Second-mile service rejected access."
                ));
            }
            if (statusCode == 400 || statusCode == 404 || statusCode == 409) {
                return new AppException(ErrorCode.INVALID_REQUEST, nonBlankOrDefault(
                        detail,
                        "Second-mile handover validation failed."
                ));
            }
            return new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Second-mile service returned HTTP " + statusCode + "."
            );
        }
        return new AppException(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Cannot connect to second-mile service. Ensure second-mile is running and SECOND_MILE_SERVICE_BASE_URL is correct."
        );
    }

    private String extractResponseDetail(RestClientResponseException exception) {
        String body = exception.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            ApiResponse<Void> response = objectMapper.readValue(body, new TypeReference<>() {
            });
            if (response.getDetail() != null && !response.getDetail().isBlank()) {
                return response.getDetail();
            }
            return response.getMessage();
        } catch (Exception parseException) {
            return null;
        }
    }

    private String nonBlankOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
