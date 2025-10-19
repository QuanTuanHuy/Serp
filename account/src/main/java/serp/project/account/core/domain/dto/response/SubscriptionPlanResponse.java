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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanResponse {

    private Long id;

    private String planName;

    private String planCode;

    private String description;

    private BigDecimal monthlyPrice;

    private BigDecimal yearlyPrice;

    private BigDecimal yearlySavings;

    private Integer maxUsers;

    private Integer trialDays;

    private Boolean isActive;

    private Boolean isCustom;

    private Long organizationId;

    private Integer displayOrder;

    /**
     * Number of modules included in this plan
     */
    private Integer moduleCount;

    private Long createdAt;

    private Long updatedAt;
}
