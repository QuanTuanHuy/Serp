/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRoleDto {
    private String name;
    private String description;
    private Boolean isRealmRole;
    private String keycloakClientId;
    private Integer priority;
    private String scope;
    private Long scopeId;
    private Long moduleId;
    private Long organizationId;
    private Long departmentId;
    private Long parentRoleId;
    private String roleType;
    private Boolean isDefault;

    private List<Long> permissionIds;
}