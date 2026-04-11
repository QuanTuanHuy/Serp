/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.infrastructure.store.mapper.ResolutionMapper;
import serp.project.pmcore.infrastructure.store.model.ResolutionModel;
import serp.project.pmcore.infrastructure.store.repository.IResolutionRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResolutionAdapter implements IResolutionPort {

    private final IResolutionRepository resolutionRepository;
    private final ResolutionMapper resolutionMapper;

    @Override
    public Optional<ResolutionEntity> getResolutionById(Long id, Long tenantId) {
        return resolutionRepository.findByIdAndTenantId(id, tenantId)
                .map(resolutionMapper::toEntity);
    }

    @Override
    public Optional<ResolutionEntity> getResolutionByIdIncludingSystem(Long id, Long tenantId) {
        return resolutionRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(resolutionMapper::toEntity);
    }

    @Override
    public Optional<ResolutionEntity> getResolutionByName(Long tenantId, String name) {
        return resolutionRepository.findFirstByTenantIdAndNameOrderByIdAsc(tenantId, name)
                .map(resolutionMapper::toEntity);
    }

    @Override
    public List<ResolutionEntity> getResolutionsByTenantId(Long tenantId) {
        return resolutionMapper.toEntities(
                resolutionRepository.findAllByTenantIdOrderBySequenceAsc(tenantId));
    }

    @Override
    public ResolutionEntity createResolution(ResolutionEntity resolution) {
        ResolutionModel savedModel = resolutionRepository.save(resolutionMapper.toModel(resolution));
        return resolutionMapper.toEntity(savedModel);
    }

    @Override
    public List<ResolutionEntity> createResolutions(List<ResolutionEntity> resolutions) {
        List<ResolutionModel> models = resolutionMapper.toModels(resolutions);
        return resolutionMapper.toEntities(resolutionRepository.saveAll(models));
    }
}
