package serp.project.school_bus_service.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Event message payload mapped from Core Account module's SyncUserEvent.
 * Received via Kafka 'SYNC_USER' topic.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUserEventMessage {

    private Long userId;
    
    private Long organizationId;

    @JsonProperty("tid")
    private Long tenantId;

    private String email;

    private String phoneNumber;

    private String firstName;

    private String lastName;

    private String fullName;

    private List<String> roleNames;

    // NOTE: The Core Account SYNC_USER event currently does not publish:
    // - keycloakId
    // - status
    // - eventType (e.g. USER_CREATED, USER_DELETED)
    // - updatedAt
    // They are left out here to reflect the exact payload structure,
    // and will be mapped with fallbacks/defaults in the consumer.

}
