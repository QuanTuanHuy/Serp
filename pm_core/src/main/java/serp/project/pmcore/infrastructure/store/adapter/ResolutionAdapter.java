/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.ResolutionMapper;
import serp.project.pmcore.infrastructure.store.model.ResolutionModel;
import serp.project.pmcore.infrastructure.store.repository.IResolutionRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

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
    public Optional<ResolutionEntity> getResolutionByNameIncludingSystem(Long tenantId, String name) {
        return resolutionRepository.findByNameAndTenantIdOrSystemTenant(name, tenantId)
                .stream()
                .findFirst()
                .map(resolutionMapper::toEntity);
    }

    @Override
    public List<ResolutionEntity> getResolutionsByTenantId(Long tenantId) {
        return resolutionMapper.toEntities(
                resolutionRepository.findAllByTenantIdOrderBySequenceAsc(tenantId));
    }

    @Override
    public PageResult<ResolutionEntity> listResolutionsIncludingSystem(Long tenantId, ResolutionListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<ResolutionModel> result = resolutionRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(resolutionMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public ResolutionEntity createResolution(ResolutionEntity resolution) {
        ResolutionModel savedModel = resolutionRepository.save(resolutionMapper.toModel(resolution));
        return resolutionMapper.toEntity(savedModel);
    }

    @Override
    public void updateResolution(ResolutionEntity resolution) {
        resolutionRepository.save(resolutionMapper.toModel(resolution));
    }

    @Override
    public List<ResolutionEntity> createResolutions(List<ResolutionEntity> resolutions) {
        List<ResolutionModel> models = resolutionMapper.toModels(resolutions);
        return resolutionMapper.toEntities(resolutionRepository.saveAll(models));
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return resolutionRepository.existsByTenantIdAndNameIgnoreCase(tenantId, name);
    }

    private Sort resolveSort(ResolutionListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "sequence" -> Sort.by(
                    new Sort.Order(direction, "sequence"),
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of sequence, name, created_at");
        };
    }
}
