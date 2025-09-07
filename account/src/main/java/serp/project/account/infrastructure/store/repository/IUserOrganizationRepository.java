/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.UserOrganizationModel;

@Repository
public interface IUserOrganizationRepository extends IBaseRepository<UserOrganizationModel> {
    List<UserOrganizationModel> findByUserId(Long userId);
}
