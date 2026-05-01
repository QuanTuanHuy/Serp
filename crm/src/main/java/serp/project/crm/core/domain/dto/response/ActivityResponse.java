/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActivityOutcome;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityResponse {
    private Long id;
    
    private String subject;
    private String description;
    
    private ActivityType activityType;
    private ActivityStatus status;
    private String location;
    
    private Long leadId;
    private Long accountId;
    private Long opportunityId;
    private Long contactId;

    private Long assignedTo;
    private String assignedToName;
    private Long activityDate;
    private Long dueDate;
    private Long reminderDate;
    private Integer durationMinutes;
    
    private TaskPriority priority;
    private Integer progressPercent;
    private ActivityOutcome outcome;
    private String notes;
    private List<String> attachments;

    private String relatedLeadName;
    private String relatedCustomerName;
    private String relatedOpportunityName;
    private String relatedContactName;

    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
