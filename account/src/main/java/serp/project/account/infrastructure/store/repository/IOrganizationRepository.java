/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.OrganizationModel;

@Repository
public interface IOrganizationRepository extends IBaseRepository<OrganizationModel> {
    Optional<OrganizationModel> findByName(String name);

    Optional<OrganizationModel> findByCode(String code);

    List<OrganizationModel> findByIdIn(List<Long> ids);
}
