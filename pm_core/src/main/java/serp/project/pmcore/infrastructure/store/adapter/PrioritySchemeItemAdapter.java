/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.core.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.PrioritySchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IPrioritySchemeItemRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrioritySchemeItemAdapter implements IPrioritySchemeItemPort {

    private final IPrioritySchemeItemRepository prioritySchemeItemRepository;
    private final PrioritySchemeItemMapper prioritySchemeItemMapper;

    @Override
    public PrioritySchemeItemEntity createPrioritySchemeItem(PrioritySchemeItemEntity item) {
        return prioritySchemeItemMapper.toEntity(prioritySchemeItemRepository.save(prioritySchemeItemMapper.toModel(item)));
    }

    @Override
    public List<PrioritySchemeItemEntity> createPrioritySchemeItems(List<PrioritySchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return prioritySchemeItemMapper.toEntities(
                prioritySchemeItemRepository.saveAll(prioritySchemeItemMapper.toModels(items))
        );
    }

    @Override
    public List<PrioritySchemeItemEntity> getPrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        return prioritySchemeItemMapper.toEntities(
                prioritySchemeItemRepository.findAllByTenantIdAndSchemeIdOrderBySequenceAsc(tenantId, schemeId)
        );
    }

    @Override
    public List<PrioritySchemeItemEntity> getPrioritySchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return prioritySchemeItemMapper.toEntities(
                prioritySchemeItemRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public void deletePrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        prioritySchemeItemRepository.deleteBySchemeIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsPriorityInScheme(Long schemeId, Long priorityId, Long tenantId) {
        return prioritySchemeItemRepository.existsByTenantIdAndSchemeIdAndPriorityId(tenantId, schemeId, priorityId);
    }
}
