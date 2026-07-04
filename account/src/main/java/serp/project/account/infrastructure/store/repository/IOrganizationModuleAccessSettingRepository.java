/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.OrganizationModuleAccessSettingModel;

@Repository
public interface IOrganizationModuleAccessSettingRepository
        extends IBaseRepository<OrganizationModuleAccessSettingModel> {

    Optional<OrganizationModuleAccessSettingModel> findByOrganizationIdAndModuleId(
            Long organizationId, Long moduleId);

    List<OrganizationModuleAccessSettingModel> findByOrganizationId(Long organizationId);

    List<OrganizationModuleAccessSettingModel> findByOrganizationIdAndAutoGrantToNewUsers(
            Long organizationId, Boolean autoGrantToNewUsers);
}
