/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationRequest;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;

@Component
@Slf4j
public class FirstMilePostOfficeCaller {

    private final RestClient restClient;
    private final AuthUtils authUtils;

    @Value("${first-mile.service.reserve-best-origin-path:/api/v1/internal/post-office-reservations/origin/best}")
    private String reserveBestOriginPath;

    @Value("${first-mile.service.reserve-drop-off-origin-path:/api/v1/internal/post-office-reservations/origin/%d/drop-off}")
    private String reserveDropOffOriginPath;

    @Value("${first-mile.service.validate-managed-post-office-path:/api/v1/internal/post-office-reservations/origin/%d/managed}")
    private String validateManagedPostOfficePath;

    public FirstMilePostOfficeCaller(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${first-mile.service.base-url:http://localhost:8093}") String firstMileBaseUrl
    ) {
        this.authUtils = authUtils;
        this.restClient = restClientBuilder.baseUrl(firstMileBaseUrl).build();
    }

    public OriginPostOfficeReservationResponse reserveBestOriginPostOffice(Double latitude, Double longitude) {
        return post(resolveReserveBestOriginPath(), buildRequest(latitude, longitude));
    }

    public OriginPostOfficeReservationResponse reserveDropOffOriginPostOffice(
            Long postOfficeId,
            Double latitude,
            Double longitude
    ) {
        return post(resolveReserveDropOffOriginPath(postOfficeId), buildRequest(latitude, longitude));
    }

    public OriginPostOfficeReservationResponse validateManagedPostOffice(Long postOfficeId) {
        String bearerToken = resolveBearerToken();
        try {
            OriginPostOfficeReservationResponse response = restClient.get()
                    .uri(resolveValidateManagedPostOfficePath(postOfficeId))
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(OriginPostOfficeReservationResponse.class);
            if (response == null) {
                throw new AppException(
                        ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "First-mile service returned empty post-office validation response."
                );
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Failed to call first-mile managed post-office validation: {}", exception.getMessage(), exception);
            throw toFirstMileException(exception);
        }
    }

    private OriginPostOfficeReservationResponse post(
            String path,
            OriginPostOfficeReservationRequest request
    ) {
        String bearerToken = resolveBearerToken();
        try {
            OriginPostOfficeReservationResponse response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(request)
                    .retrieve()
                    .body(OriginPostOfficeReservationResponse.class);
            if (response == null) {
                throw new AppException(
                        ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "First-mile service returned empty post-office reservation response."
                );
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Failed to call first-mile post-office reservation endpoint: {}", exception.getMessage(), exception);
            throw toFirstMileException(exception);
        }
    }

    private OriginPostOfficeReservationRequest buildRequest(Double latitude, Double longitude) {
        return OriginPostOfficeReservationRequest.builder()
                .senderLatitude(latitude)
                .senderLongitude(longitude)
                .build();
    }

    private String resolveBearerToken() {
        return authUtils.getBearerToken()
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNAUTHORIZED,
                        "Missing authentication token for first-mile post-office reservation."
                ));
    }

    private AppException toFirstMileException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(ErrorCode.UNAUTHORIZED, "First-mile service rejected post-office access.");
            }
            if (statusCode == 404) {
                return new AppException(ErrorCode.POST_OFFICE_NOT_FOUND);
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

    private String resolveReserveBestOriginPath() {
        return reserveBestOriginPath;
    }

    private String resolveReserveDropOffOriginPath(Long postOfficeId) {
        return String.format(reserveDropOffOriginPath, postOfficeId);
    }

    private String resolveValidateManagedPostOfficePath(Long postOfficeId) {
        return String.format(validateManagedPostOfficePath, postOfficeId);
    }
}
