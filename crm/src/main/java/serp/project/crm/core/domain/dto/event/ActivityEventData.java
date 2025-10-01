/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEventData {
    private Long activityId;
    private Long tenantId;
    private Long leadId;
    private Long contactId;
    private Long customerId;
    private Long opportunityId;
    private String activityType;
    private String subject;
    private String description;
    private String status;
    private String location;
    private Long assignedTo;
    private Long activityDate;
    private Long dueDate;
    private Long reminderDate;
    private Integer durationMinutes;
    private String priority;
    private Integer progressPercent;
    private List<String> attachments;
    private Long createdBy;
    private Long createdAt;
    private Long updatedBy;
    private Long updatedAt;
}
