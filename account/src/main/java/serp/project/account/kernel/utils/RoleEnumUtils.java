/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for working with RoleEnum
 */
public class RoleEnumUtils {

    private RoleEnumUtils() {
        // Private constructor to prevent instantiation
    }

    // ==================== ROLE QUERIES ====================

    public static List<RoleEnum> getSystemRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(RoleEnum::isSystemRole)
                .toList();
    }

    public static List<RoleEnum> getOrganizationRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(RoleEnum::isOrganizationRole)
                .toList();
    }

    public static List<RoleEnum> getModuleRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(RoleEnum::isModuleRole)
                .toList();
    }

    public static List<RoleEnum> getDepartmentRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(RoleEnum::isDepartmentRole)
                .toList();
    }

    /**
     * Get all CRM-specific roles
     */
    public static List<RoleEnum> getCrmRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getRoleName().startsWith("CRM_"))
                .toList();
    }

    /**
     * Get all PTM-specific roles
     */
    public static List<RoleEnum> getPtmRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getRoleName().startsWith("PTM_"))
                .toList();
    }

    /**
     * Get all Accounting-specific roles
     */
    public static List<RoleEnum> getAccountingRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getRoleName().startsWith("ACCOUNTING_"))
                .toList();
    }

    /**
     * Get all roles by scope
     */
    public static List<RoleEnum> getRolesByScope(RoleScope scope) {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getScope() == scope)
                .toList();
    }

    /**
     * Get all roles by type
     */
    public static List<RoleEnum> getRolesByType(RoleType type) {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getType() == type)
                .toList();
    }

    /**
     * Get all admin roles (type = ADMIN or OWNER)
     */
    public static List<RoleEnum> getAdminRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> role.getType() == RoleType.ADMIN ||
                        role.getType() == RoleType.OWNER)
                .toList();
    }

    /**
     * Get all manager roles
     */
    public static List<RoleEnum> getManagerRoles() {
        return getRolesByType(RoleType.MANAGER);
    }

    /**
     * Get all realm roles (for Keycloak)
     */
    public static List<RoleEnum> getRealmRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(RoleEnum::getIsRealmRole)
                .toList();
    }

    /**
     * Get all client roles (for Keycloak)
     */
    public static List<RoleEnum> getClientRoles() {
        return Arrays.stream(RoleEnum.values())
                .filter(role -> !role.getIsRealmRole())
                .toList();
    }

    // ==================== ROLE COMPARISONS ====================

    /**
     * Get the highest priority role from a list
     */
    public static Optional<RoleEnum> getHighestPriorityRole(List<RoleEnum> roles) {
        return roles.stream()
                .min(Comparator.comparing(RoleEnum::getPriority));
    }

    /**
     * Get the lowest priority role from a list
     */
    public static Optional<RoleEnum> getLowestPriorityRole(List<RoleEnum> roles) {
        return roles.stream()
                .max(Comparator.comparing(RoleEnum::getPriority));
    }

    /**
     * Sort roles by priority (highest first)
     */
    public static List<RoleEnum> sortByPriority(List<RoleEnum> roles) {
        return roles.stream()
                .sorted(Comparator.comparing(RoleEnum::getPriority))
                .toList();
    }

    /**
     * Check if any role in list is admin-level
     */
    public static boolean hasAdminRole(List<RoleEnum> roles) {
        return roles.stream()
                .anyMatch(role -> role.getType() == RoleType.ADMIN ||
                        role.getType() == RoleType.OWNER);
    }

    /**
     * Check if any role in list is manager-level or above
     */
    public static boolean hasManagerRole(List<RoleEnum> roles) {
        return roles.stream()
                .anyMatch(role -> role.getType() == RoleType.OWNER ||
                        role.getType() == RoleType.ADMIN ||
                        role.getType() == RoleType.MANAGER);
    }

    /**
     * Check if role list contains specific role
     */
    public static boolean containsRole(List<RoleEnum> roles, RoleEnum targetRole) {
        return roles.contains(targetRole);
    }

    /**
     * Check if role list contains any role with higher or equal priority
     */
    public static boolean hasHigherOrEqualPriority(List<RoleEnum> roles, RoleEnum compareRole) {
        return roles.stream()
                .anyMatch(role -> role.getPriority() <= compareRole.getPriority());
    }

    // ==================== ROLE VALIDATION ====================

    /**
     * Check if role is valid for organization context
     */
    public static boolean isValidForOrganization(RoleEnum role) {
        return role.isSystemRole() || role.isOrganizationRole();
    }

    /**
     * Check if role is valid for module context
     */
    public static boolean isValidForModule(RoleEnum role) {
        return role.isModuleRole();
    }

    /**
     * Check if role is valid for department context
     */
    public static boolean isValidForDepartment(RoleEnum role) {
        return role.isDepartmentRole();
    }

    /**
     * Check if role can manage users
     */
    public static boolean canManageUsers(RoleEnum role) {
        return role.getType() == RoleType.OWNER ||
                role.getType() == RoleType.ADMIN ||
                role.getType() == RoleType.MANAGER;
    }

    /**
     * Check if role can manage modules
     */
    public static boolean canManageModules(RoleEnum role) {
        return role == RoleEnum.SUPER_ADMIN ||
                role == RoleEnum.ORG_OWNER ||
                role == RoleEnum.ORG_ADMIN;
    }

    /**
     * Check if role can manage departments
     */
    public static boolean canManageDepartments(RoleEnum role) {
        return role == RoleEnum.SUPER_ADMIN ||
                role == RoleEnum.ORG_OWNER ||
                role == RoleEnum.ORG_ADMIN ||
                role == RoleEnum.DEPT_MANAGER;
    }

    // ==================== ROLE CONVERSION ====================

    /**
     * Convert role names to RoleEnum list
     */
    public static List<RoleEnum> fromRoleNames(List<String> roleNames) {
        return roleNames.stream()
                .map(RoleEnum::fromRoleName)
                .toList();
    }

    /**
     * Convert RoleEnum list to role names
     */
    public static List<String> toRoleNames(List<RoleEnum> roles) {
        return roles.stream()
                .map(RoleEnum::getRoleName)
                .toList();
    }

    /**
     * Safe conversion from role name (returns Optional)
     */
    public static Optional<RoleEnum> fromRoleNameSafe(String roleName) {
        try {
            return Optional.of(RoleEnum.fromRoleName(roleName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // ==================== ROLE SUGGESTIONS ====================

    /**
     * Suggest default role for new organization member
     */
    public static RoleEnum getDefaultOrganizationRole() {
        return RoleEnum.ORG_USER;
    }

    /**
     * Suggest default role for module based on module code
     */
    public static RoleEnum getDefaultModuleRole(String moduleCode) {
        switch (moduleCode.toUpperCase()) {
            case "CRM":
                return RoleEnum.CRM_SALES_PERSON;
            case "PTM":
                return RoleEnum.PTM_USER;
            case "ACCOUNTING":
                return RoleEnum.ACCOUNTANT;
            default:
                return RoleEnum.MODULE_USER;
        }
    }

    /**
     * Get promoted role (one level up)
     */
    public static Optional<RoleEnum> getPromotedRole(RoleEnum currentRole) {
        // CRM promotions
        if (currentRole == RoleEnum.CRM_SALES_PERSON) {
            return Optional.of(RoleEnum.CRM_SALES_MANAGER);
        }
        if (currentRole == RoleEnum.CRM_SALES_MANAGER) {
            return Optional.of(RoleEnum.CRM_ADMIN);
        }

        // PTM promotions

        // Organization promotions
        if (currentRole == RoleEnum.ORG_USER) {
            return Optional.of(RoleEnum.ORG_MODERATOR);
        }
        if (currentRole == RoleEnum.ORG_MODERATOR) {
            return Optional.of(RoleEnum.ORG_ADMIN);
        }

        // Generic promotions
        if (currentRole.getType() == RoleType.USER) {
            // Find manager role in same scope
            return getRolesByScope(currentRole.getScope()).stream()
                    .filter(role -> role.getType() == RoleType.MANAGER)
                    .findFirst();
        }

        return Optional.empty();
    }

    /**
     * Get demoted role (one level down)
     */
    public static Optional<RoleEnum> getDemotedRole(RoleEnum currentRole) {
        // CRM demotions
        if (currentRole == RoleEnum.CRM_ADMIN) {
            return Optional.of(RoleEnum.CRM_SALES_MANAGER);
        }
        if (currentRole == RoleEnum.CRM_SALES_MANAGER) {
            return Optional.of(RoleEnum.CRM_SALES_PERSON);
        }

        // PTM demotions

        // Organization demotions
        if (currentRole == RoleEnum.ORG_ADMIN) {
            return Optional.of(RoleEnum.ORG_MODERATOR);
        }
        if (currentRole == RoleEnum.ORG_MODERATOR) {
            return Optional.of(RoleEnum.ORG_USER);
        }

        return Optional.empty();
    }

    // ==================== DISPLAY HELPERS ====================

    /**
     * Get formatted display name
     */
    public static String getDisplayName(RoleEnum role) {
        // Convert CRM_SALES_PERSON to "CRM Sales Person"
        String name = role.getRoleName().replace("_", " ");
        if (name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * Get role badge color (for UI)
     */
    public static String getBadgeColor(RoleEnum role) {
        switch (role.getType()) {
            case OWNER:
                return "purple";
            case ADMIN:
                return "red";
            case MANAGER:
                return "blue";
            case USER:
                return "green";
            case VIEWER:
                return "gray";
            default:
                return "default";
        }
    }

    /**
     * Get role icon (for UI)
     */
    public static String getIcon(RoleEnum role) {
        switch (role.getType()) {
            case OWNER:
                return "crown";
            case ADMIN:
                return "shield";
            case MANAGER:
                return "star";
            case USER:
                return "user";
            case VIEWER:
                return "eye";
            default:
                return "tag";
        }
    }
}
