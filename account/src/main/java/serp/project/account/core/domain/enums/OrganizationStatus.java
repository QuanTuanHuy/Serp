/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum OrganizationStatus {
    /**
     * Organization đang hoạt động bình thường
     */
    ACTIVE,

    /**
     * Đang dùng thử (trial period)
     */
    TRIAL,

    /**
     * Bị đình chỉ (payment issues, policy violations)
     */
    SUSPENDED,

    /**
     * Hết hạn subscription
     */
    EXPIRED,

    /**
     * Đã đóng/ngừng hoạt động
     */
    CLOSED
}
