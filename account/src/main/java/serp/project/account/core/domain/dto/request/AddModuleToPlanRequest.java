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
import serp.project.account.core.domain.enums.LicenseType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddModuleToPlanRequest {

    @NotNull(message = "Module ID is required")
    private Long moduleId;

    @NotNull(message = "License type is required")
    private LicenseType licenseType;

    @Builder.Default
    private Boolean isIncluded = true;

    /**
     * Max users per module (null = inherit from plan's maxUsers)
     */
    private Integer maxUsersPerModule;
}
