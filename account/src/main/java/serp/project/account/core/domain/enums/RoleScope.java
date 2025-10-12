/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Role scope - context của role
 */
public enum RoleScope {
    /**
     * System-wide role (quản lý toàn hệ thống)
     * Ví dụ: SUPER_ADMIN
     */
    SYSTEM,

    /**
     * Organization-level role
     * Ví dụ: ORG_ADMIN, ORG_USER
     */
    ORGANIZATION,

    /**
     * Module-specific role
     * Ví dụ: CRM_SALES_MANAGER, PTM_PROJECT_MANAGER
     */
    MODULE,

    /**
     * Department-specific role
     * Ví dụ: SALES_DEPT_MANAGER, IT_DEPT_LEAD
     */
    DEPARTMENT
}
