/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "departments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "code"}),
        indexes = {
                @Index(name = "idx_departments_org_active", columnList = "organization_id, is_active"),
                @Index(name = "idx_departments_parent_id", columnList = "parent_department_id"),
                @Index(name = "idx_departments_manager_id", columnList = "manager_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class DepartmentModel extends BaseModel {
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_department_id")
    private Long parentDepartmentId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "default_module_ids", columnDefinition = "BIGINT[]")
    private Long[] defaultModuleIds;

    @Column(name = "default_role_ids", columnDefinition = "BIGINT[]")
    private Long[] defaultRoleIds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
