/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

/**
 * Billing cycle types for subscription plans
 */
public enum BillingCycle {
    /**
     * Monthly billing cycle (charged every month)
     */
    MONTHLY,

    /**
     * Yearly billing cycle (charged annually)
     */
    YEARLY,

    /**
     * Trial period (no charge, limited duration)
     */
    TRIAL
}
