/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum LicenseStatus {
    /**
     * License đang hoạt động
     */
    ACTIVE,

    /**
     * Đang trial
     */
    TRIAL,

    /**
     * Hết hạn
     */
    EXPIRED,

    /**
     * Bị đình chỉ
     */
    SUSPENDED,

    /**
     * Đã hủy
     */
    CANCELLED,

    /**
     * Đang chờ activation
     */
    PENDING
}
