/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;
import java.util.Optional;

import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;

public interface IOrganizationModuleAccessSettingService {

    OrganizationModuleAccessSettingEntity upsertAutoGrantToNewUsers(
            Long organizationId, Long moduleId, Boolean autoGrantToNewUsers, Long updatedBy);

    Optional<OrganizationModuleAccessSettingEntity> getByOrganizationIdAndModuleId(
            Long organizationId, Long moduleId);

    List<OrganizationModuleAccessSettingEntity> getByOrganizationId(Long organizationId);

    List<OrganizationModuleAccessSettingEntity> getEnabledByOrganizationId(Long organizationId);

    boolean isAutoGrantEnabled(Long organizationId, Long moduleId);
}
