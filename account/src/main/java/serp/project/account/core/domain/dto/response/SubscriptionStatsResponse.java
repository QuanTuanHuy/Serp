/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionStatsResponse {

    private Long totalSubscriptions;

    private Long activeSubscriptions;

    private Long trialSubscriptions;

    private Long expiredSubscriptions;

    private Long cancelledSubscriptions;

    private Long pendingSubscriptions;

    private BigDecimal totalRevenue;

    private BigDecimal monthlyRevenue;

    private BigDecimal yearlyRevenue;

    /**
     * Map of plan code to subscription count
     * Example: {"FREE": 50, "BASIC": 60, "PROFESSIONAL": 30}
     */
    private Map<String, Long> subscriptionsByPlan;

    /**
     * Conversion rate from trial to paid (0.0 - 1.0)
     */
    private Double conversionRate;

    /**
     * Churn rate (0.0 - 1.0)
     */
    private Double churnRate;

    /**
     * Average subscription duration in days
     */
    private Double averageSubscriptionDays;

    /**
     * Total users across all subscriptions
     */
    private Long totalUsers;

    private Long createdAt;
}
