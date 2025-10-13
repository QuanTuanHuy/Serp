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
import serp.project.account.core.domain.enums.LicenseType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleAccessResponse {

    private Long moduleId;

    private String moduleName;

    private String moduleCode;

    private String description;

    private LicenseType licenseType;

    private Boolean hasAccess;

    private Integer maxUsers;

    private Integer currentUsers;

    private Boolean canAddMoreUsers;

    private Long assignedAt;
}
