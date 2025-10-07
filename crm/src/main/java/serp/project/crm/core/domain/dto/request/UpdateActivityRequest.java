/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.*;
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
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private ActivityStatus status;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    private Long assignedTo;
    private Long activityDate;
    private Long dueDate;
    private Long reminderDate;
    
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
    
    private TaskPriority priority;
    
    @Min(value = 0, message = "Progress percent must be at least 0")
    @Max(value = 100, message = "Progress percent must not exceed 100")
    private Integer progressPercent;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
