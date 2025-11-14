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
import serp.project.account.core.domain.entity.DepartmentEntity;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentResponse {
    private Long id;
    private Long organizationId;
    private String name;
    private String code;
    private String description;
    private Long parentDepartmentId;
    private String parentDepartmentName;
    private Long managerId;
    private String managerName;
    private List<Long> defaultModuleIds;
    private List<Long> defaultRoleIds;
    private Boolean isActive;
    private Integer childrenCount;
    private Integer memberCount;
    private Long createdAt;
    private Long updatedAt;

    public DepartmentResponse(DepartmentEntity department, String parentDepartmentName, String managerName,
            Integer childrenCount, Integer memberCount) {
        this.id = department.getId();
        this.organizationId = department.getOrganizationId();
        this.name = department.getName();
        this.code = department.getCode();
        this.description = department.getDescription();
        this.parentDepartmentId = department.getParentDepartmentId();
        this.parentDepartmentName = parentDepartmentName;
        this.managerId = department.getManagerId();
        this.managerName = managerName;
        this.defaultModuleIds = department.getDefaultModuleIds();
        this.defaultRoleIds = department.getDefaultRoleIds();
        this.isActive = department.getIsActive();
        this.childrenCount = childrenCount;
        this.memberCount = memberCount;
        this.createdAt = department.getCreatedAt();
        this.updatedAt = department.getUpdatedAt();
    }
}
