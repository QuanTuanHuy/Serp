/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Permission scope - phạm vi áp dụng của permission
 */
public enum PermissionScope {
    /**
     * Chỉ có quyền với records của chính mình
     * Ví dụ: User chỉ xem được tasks của mình
     */
    OWN,

    /**
     * Có quyền với records của team/group
     * Ví dụ: Team leader xem được tasks của team members
     */
    TEAM,

    /**
     * Có quyền với records của department
     * Ví dụ: Department manager xem được tất cả tasks trong dept
     */
    DEPARTMENT,

    /**
     * Có quyền với tất cả records trong organization
     * Ví dụ: Org admin xem được tất cả tasks trong công ty
     */
    ORGANIZATION,

    /**
     * Có quyền với tất cả records (cross-organization)
     * Ví dụ: System admin xem được tất cả tasks của tất cả orgs
     */
    ALL
}
