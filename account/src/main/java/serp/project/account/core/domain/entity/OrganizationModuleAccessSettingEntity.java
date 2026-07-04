/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationModuleAccessSettingEntity extends BaseEntity {

    private Long organizationId;

    private Long moduleId;

    private Boolean autoGrantToNewUsers;

    private Long createdBy;

    private Long updatedBy;

    @JsonIgnore
    public boolean isAutoGrantEnabled() {
        return Boolean.TRUE.equals(this.autoGrantToNewUsers);
    }
}
