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
import serp.project.account.core.domain.enums.UserType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserOrganizationEntity extends BaseEntity {

    private Long userId;

    private Long organizationId;

    /**
     * ID của role ở organization-level
     */
    private Long roleId;

    private String description;

    /**
     * Organization mặc định của user không
     * User chỉ có 1 default organization
     */
    private Boolean isDefault;

    /**
     * Loại user trong organization
     */
    private UserType userType;

    /**
     * ID của department chính trong organization này
     */
    private Long departmentId;

    /**
     * Trạng thái
     * ACTIVE = đang hoạt động
     * INVITED = đã được mời nhưng chưa chấp nhận
     * SUSPENDED = bị đình chỉ
     * LEFT = đã rời khỏi organization
     */
    private String status;

    private Long invitedBy;

    private Long invitedAt;

    private Long joinedAt;

}
