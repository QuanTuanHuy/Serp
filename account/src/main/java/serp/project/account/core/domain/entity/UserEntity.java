/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserEntity extends BaseEntity {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    /**
     * Keycloak User ID
     */
    private String keycloakId;

    /**
     * System-level admin
     */
    @Builder.Default
    private Boolean isSuperAdmin = false;

    /**
     * Organization chính khi user login
     * Nếu user thuộc nhiều orgs, đây là org mặc định
     */
    private Long primaryOrganizationId;

    /**
     * Department chính của user
     */
    private Long primaryDepartmentId;

    private UserType userType;

    private UserStatus status;

    private Long lastLoginAt;

    private String avatarUrl;

    private String timezone;

    private String preferredLanguage;

    /**
     * Danh sách roles của user (global roles)
     */
    private List<RoleEntity> roles;

    /**
     * Danh sách organizations mà user tham gia
     */
    private List<UserOrganizationEntity> userOrganizations;

    /**
     * Danh sách module accesses (user có quyền vào module nào)
     */
    private List<UserModuleAccessEntity> moduleAccesses;

    /**
     * Danh sách departments mà user thuộc về
     */
    private List<UserDepartmentEntity> departments;

}
