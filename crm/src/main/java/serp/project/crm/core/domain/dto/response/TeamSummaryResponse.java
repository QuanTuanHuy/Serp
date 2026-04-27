/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.TeamStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TeamSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerUserId;
    private TeamStatus status;
    private Long memberCount;
}
