/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum UserStatus {
    /**
     * User đang hoạt động
     */
    ACTIVE,

    /**
     * User tạm ngưng (có thể reactive)
     */
    INACTIVE,

    /**
     * User đã được mời nhưng chưa activate account
     */
    INVITED,

    /**
     * User bị đình chỉ (vi phạm policy)
     */
    SUSPENDED,

    /**
     * User đã bị xóa (soft delete)
     */
    DELETED
}
