/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatchWorkItemScheduleRequest {

    @PositiveOrZero(message = "Start date must be a positive epoch timestamp")
    private Long startDate;
    private boolean startDateProvided;

    @PositiveOrZero(message = "Due date must be a positive epoch timestamp")
    private Long dueDate;
    private boolean dueDateProvided;

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
        this.startDateProvided = true;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
        this.dueDateProvided = true;
    }

    @JsonIgnore
    public UpdateWorkItemData toData() {
        Map<String, Object> systemFields = new LinkedHashMap<>();
        if (startDateProvided) {
            systemFields.put(WorkItemFieldConstants.START_DATE, startDate);
        }
        if (dueDateProvided) {
            systemFields.put(WorkItemFieldConstants.DUE_DATE, dueDate);
        }
        return new UpdateWorkItemData(systemFields, Map.of());
    }
}
