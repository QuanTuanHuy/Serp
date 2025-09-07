package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserGroupEntity;

import java.util.List;

public interface IUserGroupPort {
    void save(UserGroupEntity userGroup);
    List<UserGroupEntity> getByUserId(Long userId);
}
