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

/**
 * Quản lý quyền truy cập của user vào module cụ thể trong organization
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserModuleAccessEntity extends BaseEntity {

    private Long userId;

    private Long moduleId;

    private Long organizationId;

    private Boolean isActive;

    private Long grantedBy;

    private Long grantedAt;

    /**
     * Thời hạn của quyền truy cập (optional)
     * null = vĩnh viễn
     */
    private Long expiresAt;

    private String description;
}
