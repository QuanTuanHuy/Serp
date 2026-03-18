/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import serp.project.pmcore.domain.enums.SchemeType;

@Entity
@Table(name = "tenant_scheme_mappings")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TenantSchemeMappingModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheme_type", nullable = false)
    private SchemeType schemeType;

    @Column(name = "source_scheme_id", nullable = false)
    private Long sourceSchemeId;

    @Column(name = "tenant_scheme_id", nullable = false)
    private Long tenantSchemeId;
}
