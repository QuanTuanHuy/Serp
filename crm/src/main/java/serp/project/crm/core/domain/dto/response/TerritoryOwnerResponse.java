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
public class TerritoryOwnerResponse {
    private String territoryCode;
    private Long teamId;
    private String teamName;
    private Boolean active;
    private Long assignedAt;
    private Long assignedBy;
}
