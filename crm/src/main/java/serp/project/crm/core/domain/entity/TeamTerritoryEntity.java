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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamTerritoryEntity extends BaseEntity {
    private Long teamId;
    private String territoryCode;
    private Long assignedBy;
    private Boolean active;

    public void setDefaults() {
        if (this.active == null) {
            this.active = true;
        }
    }
}
