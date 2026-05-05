/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record HubPostOfficeMappingResponse(
        Long id,
        @JsonProperty("hub_id") Long hubId,
        @JsonProperty("post_office_code") String postOfficeCode,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("tenant_id") Long tenantId
) {
}
