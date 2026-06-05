package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class SchoolPickupPointCompatibilityResponse {

    // ── Point info ──────────────────────────────────────────────────────
    private Long pickupPointId;
    private String pickupPointCode;
    private String pickupPointName;
    private String usageType;
    private Boolean hasCoordinates;

    // ── Pickup compatibility ────────────────────────────────────────────
    /** Stable code: READY | MISSING_PICKUP_WINDOW | MISSING_COORDINATES | UNSUPPORTED_USAGE_TYPE | NOT_CHECKED */
    private String pickupReadinessCode;
    /** Human-readable label for display in UI */
    private String pickupReadinessLabel;
    /** Legacy raw status string — kept for backward compat */
    private String pickupReadinessStatus;
    private String pickupMissingConfigReason;
    private LocalTime pickupWindowStart;
    private LocalTime pickupWindowEnd;
    private Boolean compatibleForPickup;

    // ── Dropoff compatibility ───────────────────────────────────────────
    /** Stable code: READY | MISSING_DROPOFF_WINDOW | MISSING_COORDINATES | UNSUPPORTED_USAGE_TYPE | NOT_CHECKED */
    private String dropoffReadinessCode;
    /** Human-readable label for display in UI */
    private String dropoffReadinessLabel;
    /** Legacy raw status string — kept for backward compat */
    private String dropoffReadinessStatus;
    private String dropoffMissingConfigReason;
    private LocalTime dropoffWindowStart;
    private LocalTime dropoffWindowEnd;
    private Boolean compatibleForDropoff;
}
