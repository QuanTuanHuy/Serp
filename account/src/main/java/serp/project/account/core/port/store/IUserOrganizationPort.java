/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserOrganizationEntity;

import java.util.List;

public interface IUserOrganizationPort {
    UserOrganizationEntity save(UserOrganizationEntity userOrganization);

    List<UserOrganizationEntity> getByUserId(Long userId);

    UserOrganizationEntity getByUserIdAndOrganizationIdAndRoleId(Long userId, Long organizationId, Long roleId);
}
