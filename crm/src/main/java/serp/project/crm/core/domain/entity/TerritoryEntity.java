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

    public boolean isGlobal() {
        return Long.valueOf(0L).equals(this.getTenantId());
    }

    public boolean isTenantOwned() {
        return this.getTenantId() != null && this.getTenantId() > 0;
    }

    public void updateFrom(TerritoryEntity updates) {
        if (updates.getTerritoryName() != null) {
            this.territoryName = updates.getTerritoryName();
        }
        if (updates.getTerritoryLevel() != null) {
            this.territoryLevel = updates.getTerritoryLevel();
        }
        if (updates.getCountryCode() != null) {
            this.countryCode = updates.getCountryCode();
        }
        if (updates.getParentTerritoryCode() != null) {
            this.parentTerritoryCode = updates.getParentTerritoryCode();
        }
        if (updates.getActive() != null) {
            this.active = updates.getActive();
        }
    }
}
