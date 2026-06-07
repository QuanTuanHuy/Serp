package serp.project.school_bus_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import serp.project.school_bus_service.dto.response.AccountUserResponse;
import java.util.List;
import java.util.Optional;

/**
 * REST Client utilizing Spring 6 RestClient to query Account module internal APIs.
 */
@Component
@Slf4j
public class AccountUserClient {

    private final RestClient restClient;

    @Value("${school-bus.account-service.base-url:http://localhost:8081/account}")
    private String accountServiceUrl;

    public AccountUserClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Retrieve single user details by account user ID.
     */
    public Optional<AccountUserResponse> fetchUserById(Long accountUserId, String serviceToken) {
        String url = accountServiceUrl + "/internal/api/v1/users/" + accountUserId;
        log.debug("Calling Account service URL: {}", url);

        try {
            UserProfileEnvelope response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UserProfileEnvelope.class);

            if (response != null && response.data() != null) {
                return Optional.of(response.data());
            }
            log.warn("Account API returned empty payload for user ID: {}", accountUserId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Failed to fetch user ID {} from Account service. Error: {}", accountUserId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieve paginated users list sorted by updatedAt descending.
     * Passes status = "" (empty) to fetch all user statuses (ACTIVE, INACTIVE, SUSPENDED, DELETED, etc.).
     */
    public Optional<PagedResponse> fetchUsersPage(int page, int pageSize, String serviceToken) {
        String url = accountServiceUrl + "/internal/api/v1/users?page=" + page 
                + "&pageSize=" + pageSize 
                + "&sortBy=updatedAt&sortDir=desc&status=";
        log.debug("Calling Account service URL: {}", url);

        try {
            UsersPageEnvelope response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UsersPageEnvelope.class);

            if (response != null && response.data() != null) {
                return Optional.of(response.data());
            }
            log.warn("Account API returned empty users page for page: {}", page);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Failed to fetch users page {} from Account service. Error: {}", page, e.getMessage());
            return Optional.empty();
        }
    }

    // Inner Helper Envelopes for Jackson Mapping
    public record UserProfileEnvelope(
            String status,
            Integer code,
            String message,
            AccountUserResponse data
    ) {}

    public record PagedResponse(
            List<AccountUserResponse> items,
            int currentPage,
            long totalItems,
            int totalPages
    ) {}

    public record UsersPageEnvelope(
            String status,
            Integer code,
            String message,
            PagedResponse data
    ) {}

}
