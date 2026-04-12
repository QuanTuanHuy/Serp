/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.infrastructure.store.mapper.PriorityMapper;
import serp.project.pmcore.infrastructure.store.repository.IPriorityRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PriorityAdapter implements IPriorityPort {

    private final IPriorityRepository priorityRepository;
    private final PriorityMapper priorityMapper;

    @Override
    public PriorityEntity createPriority(PriorityEntity priority) {
        return priorityMapper.toEntity(priorityRepository.save(priorityMapper.toModel(priority)));
    }

    @Override
    public Optional<PriorityEntity> getPriorityById(Long id, Long tenantId) {
        return priorityRepository.findByIdAndTenantId(id, tenantId)
                .map(priorityMapper::toEntity);
    }

    @Override
    public Optional<PriorityEntity> getPriorityByIdIncludingSystem(Long id, Long tenantId) {
        return priorityRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(priorityMapper::toEntity);
    }

    @Override
    public Optional<PriorityEntity> getPriorityByPriorityKey(Long tenantId, String priorityKey) {
        return priorityRepository.findFirstByTenantIdAndPriorityKeyOrderByIdAsc(tenantId, priorityKey)
                .map(priorityMapper::toEntity);
    }

    @Override
    public List<PriorityEntity> listPriorities(Long tenantId) {
        return priorityMapper.toEntities(priorityRepository.findAllByTenantIdOrderBySequenceAsc(tenantId));
    }

    @Override
    public void updatePriority(PriorityEntity priority) {
        priorityRepository.save(priorityMapper.toModel(priority));
    }

    @Override
    public void deletePriority(Long id, Long tenantId) {
        priorityRepository.deleteByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return priorityRepository.existsByTenantIdAndName(tenantId, name);
    }
}
