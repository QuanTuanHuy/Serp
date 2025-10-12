/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Pricing models cho paid modules
 */
public enum PricingModel {
    /**
     * Tính phí theo số lượng users
     * Ví dụ: $10/user/month
     */
    PER_USER,

    /**
     * Tính phí theo organization
     * Ví dụ: $100/org/month (unlimited users)
     */
    PER_ORG,

    /**
     * Phí cố định
     * Ví dụ: $500 one-time payment
     */
    FLAT_FEE,

    /**
     * Freemium model (free với giới hạn, paid để unlock features)
     */
    FREEMIUM,

    /**
     * Tính theo usage (API calls, storage, etc.)
     */
    USAGE_BASED,

    /**
     * Miễn phí hoàn toàn
     */
    FREE
}
