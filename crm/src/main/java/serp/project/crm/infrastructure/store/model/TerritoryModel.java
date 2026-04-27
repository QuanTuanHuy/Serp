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
@Table(name = "crm_territories", indexes = {
        @Index(name = "idx_crm_territories_tenant_active", columnList = "tenant_id, is_active")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TerritoryModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "territory_code", nullable = false, length = 50)
    private String territoryCode;

    @Column(name = "territory_name", nullable = false, length = 255)
    private String territoryName;

    @Column(name = "territory_level", nullable = false, length = 30)
    private String territoryLevel;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    @Column(name = "parent_territory_code", length = 50)
    private String parentTerritoryCode;

    @Column(name = "is_active", nullable = false)
    private Boolean active;
}
