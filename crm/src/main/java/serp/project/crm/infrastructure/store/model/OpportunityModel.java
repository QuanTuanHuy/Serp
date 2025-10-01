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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "opportunities", indexes = {
        @Index(name = "idx_opportunities_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_opportunities_customer_id", columnList = "customer_id"),
        @Index(name = "idx_opportunities_lead_id", columnList = "lead_id"),
        @Index(name = "idx_opportunities_stage", columnList = "stage"),
        @Index(name = "idx_opportunities_assigned_to", columnList = "assigned_to")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OpportunityModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "stage", nullable = false, length = 50)
    private String stage;

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "probability")
    private Integer probability;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "loss_reason", columnDefinition = "TEXT")
    private String lossReason;
}
