/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

import lombok.Getter;

/**
 * Predefined system roles với context-aware metadata
 */
@Getter
public enum RoleEnum {
    // ==================== SYSTEM LEVEL ROLES ====================
    /**
     * Super Admin - Highest level, system-wide access
     * Scope: SYSTEM
     * Type: ADMIN
     * Can manage all organizations, modules, users
     */
    SUPER_ADMIN("SUPER_ADMIN", RoleScope.SYSTEM, RoleType.ADMIN, 1, true, "System Administrator with full access"),

    /**
     * System Moderator - Limited system-wide access
     * Scope: SYSTEM
     * Type: MANAGER
     * Can view system metrics, manage certain global settings
     */
    SYSTEM_MODERATOR("SYSTEM_MODERATOR", RoleScope.SYSTEM, RoleType.MANAGER, 2, true,
            "System Moderator with limited access"),

    // ==================== ORGANIZATION LEVEL ROLES ====================
    /**
     * Organization Owner - Người tạo và sở hữu organization
     * Scope: ORGANIZATION
     * Type: OWNER
     * Full control over organization (add/remove users, modules, settings)
     */
    ORG_OWNER("ORG_OWNER", RoleScope.ORGANIZATION, RoleType.OWNER, 2, true, "Organization Owner with full control"),

    /**
     * Organization Admin - Quản trị viên organization
     * Scope: ORGANIZATION
     * Type: ADMIN
     * Can manage users, roles, modules (except delete organization)
     */
    ORG_ADMIN("ORG_ADMIN", RoleScope.ORGANIZATION, RoleType.ADMIN, 3, true, "Organization Administrator"),

    /**
     * Organization Moderator - Người điều phối trong organization
     * Scope: ORGANIZATION
     * Type: MANAGER
     * Can view most data, limited management capabilities
     */
    ORG_MODERATOR("ORG_MODERATOR", RoleScope.ORGANIZATION, RoleType.MANAGER, 5, true, "Organization Moderator"),

    /**
     * Organization User - Người dùng thông thường trong organization
     * Scope: ORGANIZATION
     * Type: USER
     * Basic access, need module-specific roles for features
     */
    ORG_USER("ORG_USER", RoleScope.ORGANIZATION, RoleType.USER, 7, true, "Organization User"),

    /**
     * Organization Viewer - View-only user
     * Scope: ORGANIZATION
     * Type: VIEWER
     * Read-only access across organization
     */
    ORG_VIEWER("ORG_VIEWER", RoleScope.ORGANIZATION, RoleType.VIEWER, 8, true, "Organization Viewer (Read-only)"),

    // ==================== MODULE LEVEL ROLES ====================
    /**
     * Module Admin - Quản trị viên của một module cụ thể
     * Scope: MODULE
     * Type: ADMIN
     * Full control within the module (manage module users, settings)
     */
    MODULE_ADMIN("MODULE_ADMIN", RoleScope.MODULE, RoleType.ADMIN, 4, false, "Module Administrator"),

    /**
     * Module Manager - Quản lý trong module
     * Scope: MODULE
     * Type: MANAGER
     * Can manage module data and users
     */
    MODULE_MANAGER("MODULE_MANAGER", RoleScope.MODULE, RoleType.MANAGER, 6, false, "Module Manager"),

    /**
     * Module User - Người dùng thông thường trong module
     * Scope: MODULE
     * Type: USER
     * Standard module access
     */
    MODULE_USER("MODULE_USER", RoleScope.MODULE, RoleType.USER, 7, false, "Module User"),

    // ==================== DEPARTMENT LEVEL ROLES ====================
    /**
     * Department Manager - Quản lý phòng ban
     * Scope: DEPARTMENT
     * Type: MANAGER
     * Can manage department members and department-level permissions
     */
    DEPT_MANAGER("DEPT_MANAGER", RoleScope.DEPARTMENT, RoleType.MANAGER, 5, false, "Department Manager"),

    /**
     * Department User - Thành viên phòng ban
     * Scope: DEPARTMENT
     * Type: USER
     * Standard department member access
     */
    DEPT_USER("DEPT_USER", RoleScope.DEPARTMENT, RoleType.USER, 7, false, "Department User"),

    // ==================== SPECIFIC MODULE ROLES (Examples) ====================
    // CRM Module
    /**
     * CRM Admin - CRM Module Administrator
     */
    CRM_ADMIN("CRM_ADMIN", RoleScope.MODULE, RoleType.ADMIN, 4, false, "CRM Administrator"),

    /**
     * CRM Sales Manager - Quản lý bán hàng trong CRM
     */
    CRM_SALES_MANAGER("CRM_SALES_MANAGER", RoleScope.MODULE, RoleType.MANAGER, 6, false, "CRM Sales Manager"),

    /**
     * CRM Sales Person - Nhân viên bán hàng
     */
    CRM_SALES_PERSON("CRM_SALES_PERSON", RoleScope.MODULE, RoleType.USER, 7, false, "CRM Sales Person"),

    /**
     * CRM Viewer - View-only trong CRM
     */
    CRM_VIEWER("CRM_VIEWER", RoleScope.MODULE, RoleType.VIEWER, 8, false, "CRM Viewer"),

    // PTM (Personal Task Management) Module
    /**
     * PTM Admin - PTM Module Administrator
     */
    PTM_ADMIN("PTM_ADMIN", RoleScope.MODULE, RoleType.ADMIN, 4, false, "PTM Administrator"),

    /**
     * PTM USER
     */
    PTM_USER("PTM_USER", RoleScope.MODULE, RoleType.USER, 7, false, "PTM User"),

    // Accounting Module (future)
    /**
     * Accounting Admin
     */
    ACCOUNTING_ADMIN("ACCOUNTING_ADMIN", RoleScope.MODULE, RoleType.ADMIN, 4, false, "Accounting Administrator"),

    /**
     * Accountant - Kế toán
     */
    ACCOUNTANT("ACCOUNTANT", RoleScope.MODULE, RoleType.USER, 7, false, "Accountant"),

    /**
     * Accounting Viewer - View-only trong accounting
     */
    ACCOUNTING_VIEWER("ACCOUNTING_VIEWER", RoleScope.MODULE, RoleType.VIEWER, 8, false, "Accounting Viewer");

    /**
     * Role name (unique identifier)
     */
    private final String roleName;

    /**
     * Role scope (SYSTEM, ORGANIZATION, MODULE, DEPARTMENT)
     */
    private final RoleScope scope;

    /**
     * Role type (OWNER, ADMIN, MANAGER, USER, VIEWER, CUSTOM)
     */
    private final RoleType type;

    /**
     * Priority (1 = highest, lower number = higher priority)
     * Used for conflict resolution when user has multiple roles
     */
    private final Integer priority;

    /**
     * Is this a realm role in Keycloak?
     * true = realm role, false = client role
     */
    private final Boolean isRealmRole;

    /**
     * Role description
     */
    private final String description;

    RoleEnum(String roleName, RoleScope scope, RoleType type, Integer priority,
            Boolean isRealmRole, String description) {
        this.roleName = roleName;
        this.scope = scope;
        this.type = type;
        this.priority = priority;
        this.isRealmRole = isRealmRole;
        this.description = description;
    }

    /**
     * Get role by name
     */
    public static RoleEnum fromRoleName(String roleName) {
        for (RoleEnum role : values()) {
            if (role.roleName.equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found with name: " + roleName);
    }

    /**
     * Check if role is system-level
     */
    public boolean isSystemRole() {
        return this.scope == RoleScope.SYSTEM;
    }

    /**
     * Check if role is organization-level
     */
    public boolean isOrganizationRole() {
        return this.scope == RoleScope.ORGANIZATION;
    }

    /**
     * Check if role is module-level
     */
    public boolean isModuleRole() {
        return this.scope == RoleScope.MODULE;
    }

    /**
     * Check if role is department-level
     */
    public boolean isDepartmentRole() {
        return this.scope == RoleScope.DEPARTMENT;
    }

    /**
     * Check if this role has higher priority than another role
     * Lower priority number = higher priority
     */
    public boolean hasHigherPriorityThan(RoleEnum other) {
        return this.priority < other.priority;
    }
}
