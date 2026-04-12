/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @JsonProperty("previousAssigneeId")
    private Long previousAssigneeId;

    @JsonProperty("assignedBy")
    private Long assignedBy;

    @JsonProperty("assignedAt")
    private Long assignedAt;

    @JsonProperty("transitionId")
    private Long transitionId;

    @JsonProperty("transitionName")
    private String transitionName;

    @JsonProperty("fromStepId")
    private Long fromStepId;

    @JsonProperty("toStepId")
    private Long toStepId;

    @JsonProperty("resolutionId")
    private Long resolutionId;

    @JsonProperty("transitionedAt")
    private Long transitionedAt;

    @JsonProperty("transitionedBy")
    private Long transitionedBy;

    @JsonProperty("changedFields")
    private List<String> changedFields;
}
