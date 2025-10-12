/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Permission actions - các hành động có thể thực hiện
 */
public enum PermissionAction {
    // CRUD operations
    CREATE,
    READ,
    UPDATE,
    DELETE,

    // Additional actions
    APPROVE,
    REJECT,
    EXPORT,
    IMPORT,
    SHARE,
    PUBLISH,
    ARCHIVE,
    RESTORE,

    // Management actions
    ASSIGN,
    UNASSIGN,
    TRANSFER,

    // Special actions
    ALL // Có tất cả quyền
}
