/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import serp.project.pmcore.core.domain.enums.SchemeType;

@Entity
@Table(name = "blueprint_scheme_defaults")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BlueprintSchemeDefaultModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "blueprint_id", nullable = false)
    private Long blueprintId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheme_type", nullable = false)
    private SchemeType schemeType;

    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;
}
