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

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.PrioritySchemeItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.PrioritySchemeMapper;
import serp.project.pmcore.infrastructure.store.model.PrioritySchemeModel;
import serp.project.pmcore.infrastructure.store.repository.IPrioritySchemeItemRepository;
import serp.project.pmcore.infrastructure.store.repository.IPrioritySchemeRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PrioritySchemeAdapter implements IPrioritySchemePort {

    private final IPrioritySchemeRepository prioritySchemeRepository;
    private final IPrioritySchemeItemRepository prioritySchemeItemRepository;
    private final PrioritySchemeMapper prioritySchemeMapper;
    private final PrioritySchemeItemMapper prioritySchemeItemMapper;

    @Override
    public PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme) {
        return prioritySchemeMapper.toEntity(prioritySchemeRepository.save(prioritySchemeMapper.toModel(scheme)));
    }

    @Override
    public Optional<PrioritySchemeEntity> getPrioritySchemeById(Long schemeId, Long tenantId) {
        return prioritySchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(prioritySchemeMapper::toEntity);
    }

    @Override
    public Optional<PrioritySchemeEntity> getPrioritySchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return prioritySchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(prioritySchemeMapper::toEntity);
    }

    @Override
    public Optional<PrioritySchemeEntity> getPrioritySchemeWithItems(Long schemeId, Long tenantId) {
        return prioritySchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(model -> {
                    PrioritySchemeEntity scheme = prioritySchemeMapper.toEntity(model);
                    scheme.setItems(prioritySchemeItemMapper.toEntities(
                            prioritySchemeItemRepository.findAllByTenantIdAndSchemeIdOrderBySequenceAsc(tenantId, schemeId)
                    ));
                    return scheme;
                });
    }

    @Override
    public List<PrioritySchemeEntity> listPrioritySchemes(Long tenantId) {
        return prioritySchemeMapper.toEntities(prioritySchemeRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId));
    }

    @Override
    public PageResult<PrioritySchemeEntity> listPrioritySchemesIncludingSystem(Long tenantId,
                                                                               PrioritySchemeListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<PrioritySchemeModel> result = prioritySchemeRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(prioritySchemeMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public void updatePriorityScheme(PrioritySchemeEntity scheme) {
        prioritySchemeRepository.save(prioritySchemeMapper.toModel(scheme));
    }

    @Override
    public void deletePriorityScheme(Long schemeId, Long tenantId) {
        prioritySchemeRepository.deleteByIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return prioritySchemeRepository.existsByTenantIdAndName(tenantId, name);
    }

    @Override
    public boolean existsByDefaultPriorityId(Long priorityId, Long tenantId) {
        return prioritySchemeRepository.existsByDefaultPriorityIdAndTenantId(priorityId, tenantId);
    }

    private Sort resolveSort(PrioritySchemeListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of name, created_at");
        };
    }
}
