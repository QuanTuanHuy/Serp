/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Quản lý phòng ban trong organization
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class DepartmentEntity extends BaseEntity {
    private Long organizationId;

    private String name;

    private String code;

    private String description;

    private Long parentDepartmentId;

    private Long managerId;

    private List<Long> defaultModuleIds;

    private List<Long> defaultRoleIds;

    private Boolean isActive;

    @JsonIgnore
    public boolean isActiveDepartment() {
        return Boolean.TRUE.equals(this.isActive);
    }
}
