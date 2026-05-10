/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubPostOfficeSyncEvent {

    @JsonProperty("event_type")
    private HubPostOfficeSyncEventType eventType;

    @JsonProperty("origin")
    private HubPostOfficeSyncOrigin origin;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("hub_id")
    private Long hubId;

    @JsonProperty("post_office_code")
    private String postOfficeCode;
}
