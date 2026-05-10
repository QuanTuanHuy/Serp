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
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.StatusMapper;
import serp.project.pmcore.infrastructure.store.model.StatusModel;
import serp.project.pmcore.infrastructure.store.repository.IStatusRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StatusAdapter implements IStatusPort {

    private final IStatusRepository statusRepository;
    private final StatusMapper statusMapper;

    @Override
    public Optional<StatusEntity> getStatusById(Long id, Long tenantId) {
        return statusRepository.findByIdAndTenantId(id, tenantId)
                .map(statusMapper::toEntity);
    }

    @Override
    public Optional<StatusEntity> getStatusByIdIncludingSystem(Long id, Long tenantId) {
        return statusRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(statusMapper::toEntity);
    }

    @Override
    public Optional<StatusEntity> getStatusByStatusKey(Long tenantId, String statusKey) {
        return statusRepository.findFirstByTenantIdAndStatusKeyOrderByIdAsc(tenantId, statusKey)
                .map(statusMapper::toEntity);
    }

    @Override
    public Optional<StatusEntity> getStatusByStatusKeyIncludingSystem(Long tenantId, String statusKey) {
        return statusRepository.findByStatusKeyAndTenantIdOrSystemTenant(statusKey, tenantId)
                .stream()
                .findFirst()
                .map(statusMapper::toEntity);
    }

    @Override
    public StatusEntity createStatus(StatusEntity status) {
        return statusMapper.toEntity(statusRepository.save(statusMapper.toModel(status)));
    }

    @Override
    public void updateStatus(StatusEntity status) {
        statusRepository.save(statusMapper.toModel(status));
    }

    @Override
    public List<StatusEntity> getStatusesByTenantId(Long tenantId) {
        return statusMapper.toEntities(
                statusRepository.findAllByTenantId(tenantId));
    }

    @Override
    public List<StatusEntity> getStatusesByIds(List<Long> statusIds, Long tenantId) {
        return statusMapper.toEntities(statusRepository.findAllByIdInAndTenantId(statusIds, tenantId));
    }

    @Override
    public List<StatusEntity> getStatusesByTenantIdIncludingSystem(Long tenantId) {
        return statusMapper.toEntities(
                statusRepository.findAllByTenantIdOrSystemTenant(tenantId)
        );
    }

    @Override
    public PageResult<StatusEntity> listStatusesIncludingSystem(Long tenantId, StatusListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<StatusModel> result = statusRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getStatusCategoryId(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(statusMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public List<StatusEntity> createStatuses(List<StatusEntity> statuses) {
        List<StatusModel> models = statusMapper.toModels(statuses);
        return statusMapper.toEntities(statusRepository.saveAll(models));
    }

    @Override
    public boolean existsByCategoryId(Long categoryId, Long tenantId) {
        return statusRepository.existsByCategoryIdAndTenantId(categoryId, tenantId);
    }

    @Override
    public boolean existsByStatusKey(Long tenantId, String statusKey) {
        return statusRepository.existsByTenantIdAndStatusKeyIgnoreCase(tenantId, statusKey);
    }

    private Sort resolveSort(StatusListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "status_key" -> Sort.by(
                    new Sort.Order(direction, "statusKey"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of name, status_key, created_at");
        };
    }
}
