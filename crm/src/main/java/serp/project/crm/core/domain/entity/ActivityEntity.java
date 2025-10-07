/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ActivityEntity extends BaseEntity {
    private Long leadId;
    private Long contactId;
    private Long customerId;
    private Long opportunityId;

    private ActivityType activityType;
    private String subject;
    private String description;
    private ActivityStatus status;
    private String location;
    private Long assignedTo; // MemberId

    private Long activityDate; // Unix timestamp
    private Long dueDate;
    private Long reminderDate;

    private Integer durationMinutes;
    private TaskPriority priority;
    private Integer progressPercent;

    private List<String> attachments;

    // State management
    public boolean isCompleted() {
        return ActivityStatus.COMPLETED.equals(this.status);
    }

    public boolean isOverdue() {
        if (isCompleted() || dueDate == null) {
            return false;
        }
        return System.currentTimeMillis() > dueDate;
    }

    public void markAsCompleted(Long completedBy) {
        this.status = ActivityStatus.COMPLETED;
        this.setUpdatedBy(completedBy);
        this.progressPercent = 100;
    }

    public void markAsCancelled(Long cancelledBy) {
        if (isCompleted()) {
            throw new IllegalStateException("Cannot cancel a completed activity.");
        }

        this.status = ActivityStatus.CANCELLED;
        this.setUpdatedBy(cancelledBy);
    }

    // Scheduling
    public void reschedule(Long newDueDate, Long rescheduledBy) {
        if (isCompleted()) {
            throw new IllegalStateException("Cannot reschedule a completed activity.");
        }
        this.dueDate = newDueDate;
        this.setUpdatedBy(rescheduledBy);
    }

    public void updateFrom(ActivityEntity updates) {
        if (this.isCompleted()) {
            throw new IllegalStateException("Cannot update completed activities");
        }

        if (updates.getSubject() != null)
            this.subject = updates.getSubject();
        if (updates.getDescription() != null)
            this.description = updates.getDescription();
        if (updates.getActivityType() != null)
            this.activityType = updates.getActivityType();
        if (updates.getStatus() != null)
            this.status = updates.getStatus();
        if (updates.getLocation() != null)
            this.location = updates.getLocation();
        if (updates.getAssignedTo() != null)
            this.assignedTo = updates.getAssignedTo();
        if (updates.getActivityDate() != null)
            this.activityDate = updates.getActivityDate();
        if (updates.getDueDate() != null)
            this.dueDate = updates.getDueDate();
        if (updates.getReminderDate() != null)
            this.reminderDate = updates.getReminderDate();
        if (updates.getDurationMinutes() != null)
            this.durationMinutes = updates.getDurationMinutes();
        if (updates.getPriority() != null)
            this.priority = updates.getPriority();
        if (updates.getProgressPercent() != null)
            this.progressPercent = updates.getProgressPercent();
    }

    public void setDefaults() {
        if (this.status == null) {
            this.status = ActivityStatus.PLANNED;
        }
        if (this.progressPercent == null) {
            this.progressPercent = 0;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
    }
}
