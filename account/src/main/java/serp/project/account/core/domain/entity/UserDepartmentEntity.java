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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserDepartmentEntity extends BaseEntity {

    private Long userId;

    private Long departmentId;

    private Long organizationId;

    /**
     * Department chính của user
     * User chỉ có 1 primary department
     */
    private Boolean isPrimary;

    /**
     * Job title trong department
     * Ví dụ: "Sales Manager", "Senior Developer", "Marketing Specialist"
     */
    private String jobTitle;

    /**
     * Mô tả về vai trò trong department
     */
    private String description;

    private Boolean isActive;
}
