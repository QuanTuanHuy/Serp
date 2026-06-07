/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.TerritoryLevel;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TerritoryResponse {
    private Long id;
    private String territoryCode;
    private String territoryName;
    private TerritoryLevel territoryLevel;
    private String countryCode;
    private String parentTerritoryCode;
    private Boolean active;
    private String source;
}
