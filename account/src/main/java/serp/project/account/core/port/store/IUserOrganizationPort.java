package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserOrganizationEntity;

import java.util.List;

public interface IUserOrganizationPort {
    UserOrganizationEntity save(UserOrganizationEntity userOrganization);
    List<UserOrganizationEntity> getByUserId(Long userId);
}
