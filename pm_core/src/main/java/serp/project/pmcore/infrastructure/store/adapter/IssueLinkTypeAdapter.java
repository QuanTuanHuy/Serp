/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkTypePort;
import serp.project.pmcore.infrastructure.store.mapper.IssueLinkTypeMapper;
import serp.project.pmcore.infrastructure.store.model.IssueLinkTypeModel;
import serp.project.pmcore.infrastructure.store.repository.IIssueLinkTypeRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueLinkTypeAdapter implements IIssueLinkTypePort {

    private final IIssueLinkTypeRepository issueLinkTypeRepository;
    private final IssueLinkTypeMapper issueLinkTypeMapper;

    @Override
    public Optional<IssueLinkTypeEntity> getById(Long id, Long tenantId) {
        return issueLinkTypeRepository.findByIdAndTenantId(id, tenantId)
                .map(issueLinkTypeMapper::toEntity);
    }

    @Override
    public Optional<IssueLinkTypeEntity> getByIdIncludingSystem(Long id, Long tenantId) {
        return issueLinkTypeRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(issueLinkTypeMapper::toEntity);
    }

    @Override
    public Optional<IssueLinkTypeEntity> getByName(Long tenantId, String normalizedName) {
        return issueLinkTypeRepository.findFirstByTenantIdAndNormalizedName(tenantId, normalizedName)
                .map(issueLinkTypeMapper::toEntity);
    }

    @Override
    public List<IssueLinkTypeEntity> listByTenant(Long tenantId) {
        return issueLinkTypeMapper.toEntities(issueLinkTypeRepository.findAllByTenantIdOrderByNameAsc(tenantId));
    }

    @Override
    public List<IssueLinkTypeEntity> listByTenantIncludingSystem(Long tenantId) {
        return issueLinkTypeMapper.toEntities(issueLinkTypeRepository.findByTenantIdOrSystemTenantOrderByNameAsc(tenantId));
    }

    @Override
    public IssueLinkTypeEntity save(IssueLinkTypeEntity issueLinkType) {
        IssueLinkTypeModel saved = issueLinkTypeRepository.save(issueLinkTypeMapper.toModel(issueLinkType));
        return issueLinkTypeMapper.toEntity(saved);
    }
}
