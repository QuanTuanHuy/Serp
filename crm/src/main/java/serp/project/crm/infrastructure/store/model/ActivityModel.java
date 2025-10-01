/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activities_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_activities_assigned_to", columnList = "assigned_to"),
        @Index(name = "idx_activities_activity_type", columnList = "activity_type"),
        @Index(name = "idx_activities_due_date", columnList = "due_date"),
        @Index(name = "idx_activities_lead_id", columnList = "lead_id"),
        @Index(name = "idx_activities_customer_id", columnList = "customer_id"),
        @Index(name = "idx_activities_opportunity_id", columnList = "opportunity_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ActivityModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "assigned_to", nullable = false)
    private Long assignedTo;

    @Column(name = "activity_date")
    private Long activityDate;

    @Column(name = "due_date")
    private Long dueDate;

    @Column(name = "reminder_date")
    private Long reminderDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;
}
