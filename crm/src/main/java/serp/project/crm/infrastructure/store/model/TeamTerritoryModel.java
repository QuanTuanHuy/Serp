/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "crm_team_territories", indexes = {
        @Index(name = "idx_crm_team_territories_team_active", columnList = "team_id, is_active")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamTerritoryModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "territory_code", nullable = false, length = 50)
    private String territoryCode;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "assigned_by")
    private Long assignedBy;
}
