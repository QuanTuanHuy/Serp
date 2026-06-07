/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.HandoverManifestStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverManifestFilterRequest {
    @JsonProperty("origin_post_office_code")
    private String originPostOfficeCode;

    @JsonProperty("target_hub_id")
    private Long targetHubId;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    private HandoverManifestStatus status;
}
