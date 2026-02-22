/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.core.port.store.IPrioritySchemePort;
import serp.project.pmcore.infrastructure.store.mapper.PrioritySchemeItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.PrioritySchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IPrioritySchemeItemRepository;
import serp.project.pmcore.infrastructure.store.repository.IPrioritySchemeRepository;

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
}
