/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.OpportunityStage;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PipelineStageResponse {
    private OpportunityStage stage;
    private Integer count;
    private BigDecimal totalValue;
    private BigDecimal weightedValue;
    private List<OpportunityResponse> opportunities;
}
