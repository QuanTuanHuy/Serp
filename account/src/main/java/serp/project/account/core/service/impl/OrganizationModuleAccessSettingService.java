/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.core.port.store.IOrganizationModuleAccessSettingPort;
import serp.project.account.core.service.IOrganizationModuleAccessSettingService;

@Service
@RequiredArgsConstructor
public class OrganizationModuleAccessSettingService implements IOrganizationModuleAccessSettingService {

    private final IOrganizationModuleAccessSettingPort port;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationModuleAccessSettingEntity upsertAutoGrantToNewUsers(
            Long organizationId, Long moduleId, Boolean autoGrantToNewUsers, Long updatedBy) {
        var setting = port.findByOrganizationIdAndModuleId(organizationId, moduleId)
                .orElseGet(() -> OrganizationModuleAccessSettingEntity.builder()
                        .organizationId(organizationId)
                        .moduleId(moduleId)
                        .createdBy(updatedBy)
                        .build());
        setting.setAutoGrantToNewUsers(Boolean.TRUE.equals(autoGrantToNewUsers));
        setting.setUpdatedBy(updatedBy);
        return port.save(setting);
    }

    @Override
    public Optional<OrganizationModuleAccessSettingEntity> getByOrganizationIdAndModuleId(
            Long organizationId, Long moduleId) {
        return port.findByOrganizationIdAndModuleId(organizationId, moduleId);
    }

    @Override
    public List<OrganizationModuleAccessSettingEntity> getByOrganizationId(Long organizationId) {
        return port.findByOrganizationId(organizationId);
    }

    @Override
    public List<OrganizationModuleAccessSettingEntity> getEnabledByOrganizationId(Long organizationId) {
        return port.findEnabledByOrganizationId(organizationId);
    }

    @Override
    public boolean isAutoGrantEnabled(Long organizationId, Long moduleId) {
        return port.findByOrganizationIdAndModuleId(organizationId, moduleId)
                .map(OrganizationModuleAccessSettingEntity::isAutoGrantEnabled)
                .orElse(false);
    }
}
