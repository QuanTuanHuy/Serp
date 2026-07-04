/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;
import java.util.Optional;

import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;

public interface IOrganizationModuleAccessSettingPort {

    OrganizationModuleAccessSettingEntity save(OrganizationModuleAccessSettingEntity setting);

    Optional<OrganizationModuleAccessSettingEntity> findByOrganizationIdAndModuleId(
            Long organizationId, Long moduleId);

    List<OrganizationModuleAccessSettingEntity> findByOrganizationId(Long organizationId);

    List<OrganizationModuleAccessSettingEntity> findEnabledByOrganizationId(Long organizationId);
}
