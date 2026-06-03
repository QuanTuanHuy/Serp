/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.tms_order.dto.request.InternalPostOfficeSuggestionRequest;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;

import java.util.List;

@Component
@Slf4j
public class FirstMilePostOfficeSuggestionCaller {

    private final RestClient restClient;
    private final AuthUtils authUtils;

    @Value("${first-mile.service.drop-off-suggestions-path:/api/v1/internal/post-office-suggestions/drop-off}")
    private String dropOffSuggestionsPath;

    public FirstMilePostOfficeSuggestionCaller(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${first-mile.service.base-url:http://localhost:8093}") String firstMileBaseUrl
    ) {
        this.authUtils = authUtils;
        this.restClient = restClientBuilder.baseUrl(firstMileBaseUrl).build();
    }

    public List<OrderDropOffPostOfficeSuggestionResponse> getDropOffSuggestions(
            Double senderLatitude,
            Double senderLongitude,
            Integer limit
    ) {
        String bearerToken = resolveBearerToken();
        InternalPostOfficeSuggestionRequest request = InternalPostOfficeSuggestionRequest.builder()
                .senderLatitude(senderLatitude)
                .senderLongitude(senderLongitude)
                .limit(limit)
                .build();
        try {
            List<OrderDropOffPostOfficeSuggestionResponse> response = restClient.post()
                    .uri(dropOffSuggestionsPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OrderDropOffPostOfficeSuggestionResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientException exception) {
            log.error("Failed to call first-mile drop-off suggestion endpoint: {}", exception.getMessage(), exception);
            throw toFirstMileException(exception);
        }
    }

    private String resolveBearerToken() {
        return authUtils.getBearerToken()
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNAUTHORIZED,
                        "Missing authentication token for first-mile post-office suggestions."
                ));
    }

    private AppException toFirstMileException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(ErrorCode.UNAUTHORIZED, "First-mile service rejected post-office access.");
            }
            if (statusCode == 400) {
                return new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE);
            }
            return new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "First-mile service returned HTTP " + statusCode + "."
            );
        }
        return new AppException(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Cannot connect to first-mile service. Ensure first-mile is running and FIRST_MILE_SERVICE_BASE_URL is correct."
        );
    }
}
