/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
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
public class SubscribeCustomPlanRequest {
    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @Builder.Default
    private Boolean isAutoRenew = false;

    @Max(value = 255, message = "Notes cannot exceed 255 characters")
    private String notes;

    private List<Long> moduleIds;

    public SubscribeRequest toSubscribeRequest(Long planId) {
        return SubscribeRequest.builder()
                .planId(planId)
                .billingCycle(this.billingCycle)
                .isAutoRenew(this.isAutoRenew)
                .notes(this.notes)
                .build();
    }
}
