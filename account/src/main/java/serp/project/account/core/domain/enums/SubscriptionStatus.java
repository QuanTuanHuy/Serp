/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Status của subscription
 */
public enum SubscriptionStatus {
    /**
     * Subscription đang active
     */
    ACTIVE,

    /**
     * Đang trong thời gian trial
     */
    TRIAL,

    /**
     * Hết hạn
     */
    EXPIRED,

    /**
     * Đã hủy
     */
    CANCELLED,

    /**
     * Payment failed (cho tương lai)
     */
    PAYMENT_FAILED,

    /**
     * Grace period (thời gian gia hạn sau khi hết hạn)
     */
    GRACE_PERIOD,

    /**
     * Pending activation (chờ admin approve)
     */
    PENDING,

    /**
     * Pending upgrade (chờ nâng cấp)
     */
    PENDING_UPGRADE,
}
