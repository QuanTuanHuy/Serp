/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberReassignmentResponse {
    private Long sourceUserId;
    private Long targetUserId;
    private Integer reassignedLeads;
    private Integer reassignedOpportunities;
    private Integer reassignedActivities;
}
