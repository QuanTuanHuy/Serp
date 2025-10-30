/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.UserModel;

@Repository
public interface IUserRepository extends IBaseRepository<UserModel> {
    Optional<UserModel> findByEmail(String email);

    List<UserModel> findByIdIn(List<Long> ids);

    List<UserModel> findByPrimaryOrganizationId(Long organizationId);
}
