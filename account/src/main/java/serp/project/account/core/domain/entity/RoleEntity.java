/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;

/**
 * Context-aware Role entity
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RoleEntity extends BaseEntity {

    private String name;

    private String description;

    private Boolean isRealmRole;

    /**
     * Keycloak Client ID (nếu là client role)
     */
    private String keycloakClientId;

    /**
     * Độ ưu tiên của role
     */
    private Integer priority;

    /**
     * Scope/Context của role
     */
    private RoleScope scope;

    /**
     * ID của scope (tùy thuộc vào loại scope)
     * - Nếu scope = ORGANIZATION → scopeId = organizationId
     * - Nếu scope = MODULE → scopeId = moduleId
     */
    private Long scopeId;

    /**
     * ID của module mà role này thuộc về
     * null = global role (không thuộc module cụ thể)
     */
    private Long moduleId;

    /**
     * ID của organization mà role này thuộc về
     * null = system role (available for all organizations)
     */
    private Long organizationId;

    /**
     * ID của department mà role này thuộc về
     */
    private Long departmentId;

    /**
     * Parent role ID (hỗ trợ role inheritance)
     * Ví dụ: "Sales Manager" inherits permissions from "Sales Person"
     */
    private Long parentRoleId;

    /**
     * Loại role
     */
    private RoleType roleType;

    /**
     * Auto-assign role này cho user mới trong org/module không
     * true = tự động assign khi user join vào org/module
     */
    private Boolean isDefault;

    /**
     * Danh sách permissions của role
     */
    private List<PermissionEntity> permissions;

    /**
     * Danh sách menu displays (UI menus) mà role này có quyền xem
     */
    private List<MenuDisplayEntity> menuDisplays;

    public boolean isSystemRole() {
        return RoleScope.SYSTEM.equals(this.scope);
    }

    public boolean isOrganizationRole() {
        return RoleScope.ORGANIZATION.equals(this.scope);
    }

    public boolean isModuleRole() {
        return RoleScope.MODULE.equals(this.scope);
    }

    public boolean isDepartmentRole() {
        return RoleScope.DEPARTMENT.equals(this.scope);
    }

    public boolean hasHigherPriorityThan(RoleEntity other) {
        if (other == null || this.priority == null || other.priority == null) {
            return false;
        }
        return this.priority < other.priority;
    }

    public boolean isKeycloakRealmRole() {
        return Boolean.TRUE.equals(this.isRealmRole);
    }

    public boolean isKeycloakClientRole() {
        return Boolean.FALSE.equals(this.isRealmRole);
    }

    public boolean isAutoAssigned() {
        return Boolean.TRUE.equals(this.isDefault);
    }

    public boolean hasPermissions() {
        return this.permissions != null && !this.permissions.isEmpty();
    }

    public boolean hasMenuDisplays() {
        return this.menuDisplays != null && !this.menuDisplays.isEmpty();
    }

    public int getPermissionCount() {
        return this.permissions != null ? this.permissions.size() : 0;
    }

    public int getMenuDisplayCount() {
        return this.menuDisplays != null ? this.menuDisplays.size() : 0;
    }

    public boolean hasParentRole() {
        return this.parentRoleId != null;
    }

    public boolean isValid() {
        return this.name != null && !this.name.trim().isEmpty() 
            && this.scope != null 
            && this.roleType != null
            && this.isRealmRole != null
            && this.priority != null && this.priority > 0;
    }

    public String getDisplayName() {
        if (this.description != null && !this.description.trim().isEmpty()) {
            return this.description;
        }
        return this.name;
    }
}
