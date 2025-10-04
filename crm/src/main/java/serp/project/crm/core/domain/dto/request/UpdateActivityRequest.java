/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.TaskPriority;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateActivityRequest {
    private String subject;
    private String description;
    
    private ActivityStatus status;
    private String location;
    
    private Long assignedTo;
    private Long activityDate;
    private Long dueDate;
    private Long reminderDate;
    private Integer durationMinutes;
    
    private TaskPriority priority;
    private Integer progressPercent;
    private String notes;
}
