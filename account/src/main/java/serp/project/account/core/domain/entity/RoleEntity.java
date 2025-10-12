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
}
