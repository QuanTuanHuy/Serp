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
import serp.project.crm.core.domain.enums.LeadStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadStatusTransitionResponse {
    private LeadResponse lead;
    private LeadStatus fromStatus;
    private LeadStatus toStatus;
    private Long accountId;
    private Long opportunityId;
    private Long contactId;
    private String message;
}
