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
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;

@Entity
@Table(name = "issue_link_types")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class IssueLinkTypeModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "outward_desc", nullable = false, length = 100)
    private String outwardDescription;

    @Column(name = "inward_desc", nullable = false, length = 100)
    private String inwardDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_behavior", nullable = false, length = 50)
    private IssueLinkDependencyBehavior dependencyBehavior;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem;
}
