/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateWorkItemRequest {

    private String summary;
    private boolean summaryProvided;
    private String description;
    private boolean descriptionProvided;
    private Long priorityId;
    private boolean priorityIdProvided;
    private Long assigneeId;
    private boolean assigneeIdProvided;
    private Long startDate;
    private boolean startDateProvided;
    private Long dueDate;
    private boolean dueDateProvided;
    private Long timeOriginalEstimate;
    private boolean timeOriginalEstimateProvided;
    private Long timeRemainingEstimate;
    private boolean timeRemainingEstimateProvided;
    private Long securityLevelId;
    private boolean securityLevelIdProvided;
    private Map<String, Object> customFields = Map.of();
    private boolean customFieldsProvided;

    public void setSummary(String summary) {
        this.summary = summary;
        this.summaryProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setPriorityId(Long priorityId) {
        this.priorityId = priorityId;
        this.priorityIdProvided = true;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
        this.assigneeIdProvided = true;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
        this.startDateProvided = true;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
        this.dueDateProvided = true;
    }

    public void setTimeOriginalEstimate(Long timeOriginalEstimate) {
        this.timeOriginalEstimate = timeOriginalEstimate;
        this.timeOriginalEstimateProvided = true;
    }

    public void setTimeRemainingEstimate(Long timeRemainingEstimate) {
        this.timeRemainingEstimate = timeRemainingEstimate;
        this.timeRemainingEstimateProvided = true;
    }

    public void setSecurityLevelId(Long securityLevelId) {
        this.securityLevelId = securityLevelId;
        this.securityLevelIdProvided = true;
    }

    public void setCustomFields(Map<String, Object> customFields) {
        this.customFields = customFields == null ? Map.of() : new LinkedHashMap<>(customFields);
        this.customFieldsProvided = true;
    }

    @JsonIgnore
    public UpdateWorkItemData toData() {
        Map<String, Object> systemFields = new LinkedHashMap<>();
        if (summaryProvided) {
            systemFields.put(WorkItemFieldConstants.SUMMARY, summary);
        }
        if (descriptionProvided) {
            systemFields.put(WorkItemFieldConstants.DESCRIPTION, description);
        }
        if (priorityIdProvided) {
            systemFields.put(WorkItemFieldConstants.PRIORITY_ID, priorityId);
        }
        if (assigneeIdProvided) {
            systemFields.put(WorkItemFieldConstants.ASSIGNEE_ID, assigneeId);
        }
        if (startDateProvided) {
            systemFields.put(WorkItemFieldConstants.START_DATE, startDate);
        }
        if (dueDateProvided) {
            systemFields.put(WorkItemFieldConstants.DUE_DATE, dueDate);
        }
        if (timeOriginalEstimateProvided) {
            systemFields.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, timeOriginalEstimate);
        }
        if (timeRemainingEstimateProvided) {
            systemFields.put(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE, timeRemainingEstimate);
        }
        if (securityLevelIdProvided) {
            systemFields.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, securityLevelId);
        }

        return new UpdateWorkItemData(
                systemFields,
                customFieldsProvided ? customFields : Map.of()
        );
    }
}
