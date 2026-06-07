/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;

public record HubStaffResponse(
        Long id,
        String code,
        @JsonProperty("full_name") String fullName,
        HubStaffRole role,
        HubStaffStatus status
) {
}
