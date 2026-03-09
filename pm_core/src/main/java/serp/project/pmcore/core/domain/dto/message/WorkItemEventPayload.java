/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemEventPayload {

    @JsonProperty("workItemId")
    private Long workItemId;

    @JsonProperty("workItemKey")
    private String workItemKey;

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("issueTypeId")
    private Long issueTypeId;

    @JsonProperty("statusId")
    private Long statusId;

    @JsonProperty("assigneeId")
    private Long assigneeId;
}
