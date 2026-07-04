/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.core.port.store.IOrganizationModuleAccessSettingPort;
import serp.project.account.infrastructure.store.mapper.OrganizationModuleAccessSettingMapper;
import serp.project.account.infrastructure.store.repository.IOrganizationModuleAccessSettingRepository;

@Component
@RequiredArgsConstructor
public class OrganizationModuleAccessSettingAdapter implements IOrganizationModuleAccessSettingPort {

    private final IOrganizationModuleAccessSettingRepository repository;
    private final OrganizationModuleAccessSettingMapper mapper;

    @Override
    public OrganizationModuleAccessSettingEntity save(OrganizationModuleAccessSettingEntity setting) {
        return mapper.toEntity(repository.save(mapper.toModel(setting)));
    }

    @Override
    public Optional<OrganizationModuleAccessSettingEntity> findByOrganizationIdAndModuleId(
            Long organizationId, Long moduleId) {
        return repository.findByOrganizationIdAndModuleId(organizationId, moduleId)
                .map(mapper::toEntity);
    }

    @Override
    public List<OrganizationModuleAccessSettingEntity> findByOrganizationId(Long organizationId) {
        return mapper.toEntityList(repository.findByOrganizationId(organizationId));
    }

    @Override
    public List<OrganizationModuleAccessSettingEntity> findEnabledByOrganizationId(Long organizationId) {
        return mapper.toEntityList(
                repository.findByOrganizationIdAndAutoGrantToNewUsers(organizationId, true));
    }
}
