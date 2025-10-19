/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.account.core.domain.enums.BillingCycle;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradeSubscriptionRequest {

    @NotNull(message = "New plan ID is required")
    private Long newPlanId;

    private BillingCycle billingCycle;

    @Builder.Default
    private Boolean isAutoRenew = false;

    private String notes;
}
