/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.OpportunityStage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OpportunityFilterRequest extends BaseFilterRequest {

    private String keyword;
    private List<OpportunityStage> stages;
    private Long customerId;
    private Long leadId;
    private Long assignedTo;
    private Boolean unassignedOnly;

    private BigDecimal estimatedValueMin;
    private BigDecimal estimatedValueMax;

    private Integer probabilityMin;
    private Integer probabilityMax;

    private LocalDate expectedCloseDateFrom;
    private LocalDate expectedCloseDateTo;

    private LocalDate actualCloseDateFrom;
    private LocalDate actualCloseDateTo;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;

    private Boolean hasNotes;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
