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
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanDetailResponse {

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

    private List<PlanModuleResponse> modules;

    private Map<String, String> features;

    private Long createdAt;

    private Long updatedAt;

    private Long createdBy;

    private Long updatedBy;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PlanModuleResponse {
        private Long id;

        private Long moduleId;

        private String moduleName;

        private String moduleCode;

        private Boolean isIncluded;

        private String licenseType;

        private Integer maxUsersPerModule;
    }
}
