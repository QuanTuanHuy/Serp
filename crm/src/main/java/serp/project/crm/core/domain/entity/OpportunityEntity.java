/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.OpportunityStage;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OpportunityEntity extends BaseEntity {
    private String opportunityCode;
    private Long customerId;
    private Long leadId;
    private String name;
    private String description;
    private OpportunityStage stage;
    private BigDecimal estimatedValue;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String competitors;
    private String notes;
    private String lossReason;
}
