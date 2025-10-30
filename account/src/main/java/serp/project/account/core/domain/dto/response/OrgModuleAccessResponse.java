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
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgModuleAccessResponse {
    private Long organizationId;
    private Long moduleId;
    private String moduleName;
    private String moduleCode;
    private String moduleDescription;
    private Boolean isActive;
    private Long grantedAt;
    private Integer activeUserCount;
    private Integer totalUsersCount;
    @Builder.Default
    private Boolean isAutoGrantToNewUsers = false;
    private List<String> requiredRoles;
}
