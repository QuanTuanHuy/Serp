/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum UserType {
    /**
     * Chủ doanh nghiệp (người tạo organization)
     */
    OWNER,

    /**
     * Quản trị viên (được owner chỉ định)
     */
    ADMIN,

    /**
     * Nhân viên chính thức
     */
    EMPLOYEE,

    /**
     * Nhà thầu/freelancer (temporary access)
     */
    CONTRACTOR,

    /**
     * Người dùng external (partners, clients, vendors)
     */
    EXTERNAL,

    /**
     * Guest user (limited access, view-only)
     */
    GUEST
}
