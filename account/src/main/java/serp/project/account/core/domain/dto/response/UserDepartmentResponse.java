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
import serp.project.account.core.domain.entity.UserDepartmentEntity;
import serp.project.account.core.domain.entity.UserEntity;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDepartmentResponse {
    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private Long departmentId;
    private Boolean isPrimary;
    private String jobTitle;
    private String description;
    private Boolean isActive;

    private Long createdAt;
    private Long updatedAt;

    public UserDepartmentResponse(UserDepartmentEntity userDepartment, UserEntity user) {
        this.userId = userDepartment.getUserId();
        this.userName = user != null ? user.getFullName() : "";
        this.email = user != null ? user.getEmail() : "";
        this.phoneNumber = user != null ? user.getPhoneNumber() : "";
        this.departmentId = userDepartment.getDepartmentId();
        this.isPrimary = userDepartment.getIsPrimary();
        this.jobTitle = userDepartment.getJobTitle();
        this.description = userDepartment.getDescription();
        this.isActive = userDepartment.getIsActive();
        this.createdAt = userDepartment.getCreatedAt();
        this.updatedAt = userDepartment.getUpdatedAt();
    }
}
