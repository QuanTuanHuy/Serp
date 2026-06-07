package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entity representing the shadow/cached user data synchronized from Core Account module.
 */
@Entity
@Table(name = "school_bus_user")
@Getter
@Setter
public class SchoolBusUserEntity extends BaseModel {

    /**
     * Map directly to users.id from the Core Account module.
     * Serve as the main reference for parent/driver/attendant profile links.
     */
    @Column(name = "account_user_id", nullable = false)
    private Long accountUserId;

    /**
     * Used to match with Keycloak subject (sub claim) in JWT tokens if needed.
     */
    @Column(name = "keycloak_id")
    private String keycloakId;

    @Column(nullable = false)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "primary_organization_id")
    private Long primaryOrganizationId;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    private String timezone;

    @Column(name = "user_type")
    private String userType;

    private String status;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "sync_source")
    private String syncSource;

    @Column(name = "raw_payload_json", columnDefinition = "TEXT")
    private String rawPayloadJson;

}
