/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ActivityResponse {
    private Long id;
    
    private String subject;
    private String description;
    
    private ActivityType activityType;
    private ActivityStatus status;
    private String location;
    
    private Long relatedToLeadId;
    private Long relatedToCustomerId;
    private Long relatedToOpportunityId;
    private Long relatedToContactId;
    
    private Long assignedTo;
    private Long activityDate;
    private Long dueDate;
    private Long reminderDate;
    private Integer durationMinutes;
    
    private TaskPriority priority;
    private Integer progressPercent;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
