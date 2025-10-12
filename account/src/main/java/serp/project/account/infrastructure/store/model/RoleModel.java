/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RoleModel extends BaseModel {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_realm_role", nullable = false)
    private Boolean isRealmRole;

    @Column(name = "keycloak_client_id", length = 100)
    private String keycloakClientId;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "scope", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleScope scope;

    @Column(name = "scope_id")
    private Long scopeId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "parent_role_id")
    private Long parentRoleId;

    @Column(name = "role_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column(name = "is_default")
    private Boolean isDefault;
}
