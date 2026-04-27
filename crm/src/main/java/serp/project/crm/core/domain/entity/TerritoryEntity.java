/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.TerritoryLevel;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TerritoryEntity extends BaseEntity {
    private String territoryCode;
    private String territoryName;
    private TerritoryLevel territoryLevel;
    private String countryCode;
    private String parentTerritoryCode;
    private Boolean active;

    public void setDefaults() {
        if (this.countryCode == null || this.countryCode.isBlank()) {
            this.countryCode = "VN";
        }
        if (this.territoryLevel == null) {
            this.territoryLevel = TerritoryLevel.PROVINCE_CITY;
        }
        if (this.active == null) {
            this.active = true;
        }
    }
}
