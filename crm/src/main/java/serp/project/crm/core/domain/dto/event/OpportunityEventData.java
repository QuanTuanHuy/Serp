/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityEventData {
    private Long opportunityId;
    private Long tenantId;
    private String name;
    private String description;
    private Long leadId;
    private Long customerId;
    private String customerName;
    private String stage;
    private String previousStage;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String notes;
    private String lossReason;
    private Long createdBy;
    private Long createdAt;
    private Long updatedBy;
    private Long updatedAt;
}
