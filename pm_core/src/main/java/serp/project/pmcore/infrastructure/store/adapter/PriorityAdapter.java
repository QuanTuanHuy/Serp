/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.PriorityMapper;
import serp.project.pmcore.infrastructure.store.model.PriorityModel;
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
    public List<PriorityEntity> listPrioritiesIncludingSystem(Long tenantId) {
        return priorityMapper.toEntities(priorityRepository.findAllByTenantIdOrSystemTenant(tenantId));
    }

    @Override
    public PageResult<PriorityEntity> listPrioritiesIncludingSystem(Long tenantId, PriorityListCriteria criteria) {
        Pageable pageable = PageRequest.of(resolvePage(criteria), resolvePageSize(criteria), resolveSort(criteria));
        Page<PriorityModel> result = priorityRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(priorityMapper.toEntities(result.getContent()), result.getTotalElements());
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
        return priorityRepository.existsByTenantIdAndNameIgnoreCase(tenantId, name);
    }

    private int resolvePage(PriorityListCriteria criteria) {
        int page = criteria.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolvePageSize(PriorityListCriteria criteria) {
        int pageSize = criteria.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private Sort resolveSort(PriorityListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        String sortDirection = criteria.getSortDirection().toUpperCase();

        Sort.Direction direction = switch (sortDirection) {
            case "ASC" -> Sort.Direction.ASC;
            case "DESC" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("sortDirection must be ASC or DESC");
        };

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
