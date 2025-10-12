/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Role types - phân loại roles theo mức độ quyền hạn
 */
public enum RoleType {
    /**
     * Chủ sở hữu (full control, không thể bị revoke)
     */
    OWNER,

    /**
     * Quản trị viên (almost full control)
     */
    ADMIN,

    /**
     * Quản lý (department/module level)
     */
    MANAGER,

    /**
     * Người dùng thông thường
     */
    USER,

    /**
     * View-only user (read access only)
     */
    VIEWER,

    /**
     * Custom role (tùy chỉnh)
     */
    CUSTOM
}
