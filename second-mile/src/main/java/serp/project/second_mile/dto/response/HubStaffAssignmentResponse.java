/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HubStaffAssignmentResponse(
        Long id,
        @JsonProperty("hub_id") Long hubId,
        @JsonProperty("hub_code") String hubCode,
        @JsonProperty("hub_name") String hubName,
        @JsonProperty("staff_id") Long staffId,
        @JsonProperty("staff_code") String staffCode,
        @JsonProperty("staff_full_name") String staffFullName,
        @JsonProperty("staff_role") HubStaffRole staffRole,
        @JsonProperty("staff_status") HubStaffStatus staffStatus,
        @JsonProperty("assigned_from") LocalDate assignedFrom,
        @JsonProperty("assigned_to") LocalDate assignedTo,
        @JsonProperty("is_primary") Boolean isPrimary,
        @JsonProperty("notes") String notes,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
