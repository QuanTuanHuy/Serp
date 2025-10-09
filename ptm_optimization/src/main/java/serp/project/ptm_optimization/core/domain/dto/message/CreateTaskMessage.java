package serp.project.ptm_optimization.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.ptm_optimization.core.domain.enums.ActiveStatusEnum;
import serp.project.ptm_optimization.core.domain.enums.PriorityEnum;
import serp.project.ptm_optimization.core.domain.enums.TaskStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTaskMessage {
    private Long projectId;
    private Long groupTaskId;
    private Long taskId;
    private Long userId;
    private String title;
    private String description;
    private PriorityEnum[] priority;
    private TaskStatusEnum status;
    private Long startDate;
    private Long deadline;
    private Double duration;
    private ActiveStatusEnum activeStatus;
}
