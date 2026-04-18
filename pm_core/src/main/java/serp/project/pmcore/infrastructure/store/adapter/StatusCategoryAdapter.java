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
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.StatusCategoryMapper;
import serp.project.pmcore.infrastructure.store.model.StatusCategoryModel;
import serp.project.pmcore.infrastructure.store.repository.IStatusCategoryRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StatusCategoryAdapter implements IStatusCategoryPort {

    private final IStatusCategoryRepository statusCategoryRepository;
    private final StatusCategoryMapper statusCategoryMapper;

    @Override
    public Optional<StatusCategoryEntity> getStatusCategoryById(Long id, Long tenantId) {
        return statusCategoryRepository.findByIdAndTenantId(id, tenantId)
                .map(statusCategoryMapper::toEntity);
    }

    @Override
    public Optional<StatusCategoryEntity> getStatusCategoryByIdIncludingSystem(Long id, Long tenantId) {
        return statusCategoryRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(statusCategoryMapper::toEntity);
    }

    @Override
    public Optional<StatusCategoryEntity> getStatusCategoryByKey(Long tenantId, String key) {
        return statusCategoryRepository.findFirstByTenantIdAndKeyOrderByIdAsc(tenantId, key)
                .map(statusCategoryMapper::toEntity);
    }

    @Override
    public Optional<StatusCategoryEntity> getStatusCategoryByKeyIncludingSystem(Long tenantId, String key) {
        return statusCategoryRepository.findByKeyAndTenantIdOrSystemTenant(key, tenantId)
                .stream()
                .findFirst()
                .map(statusCategoryMapper::toEntity);
    }

    @Override
    public PageResult<StatusCategoryEntity> listStatusCategoriesIncludingSystem(Long tenantId,
                                                                                StatusCategoryListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<StatusCategoryModel> result = statusCategoryRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        List<StatusCategoryEntity> entities = result.getContent().stream()
                .map(statusCategoryMapper::toEntity)
                .toList();
        return new PageResult<>(entities, result.getTotalElements());
    }

    @Override
    public StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory) {
        return statusCategoryMapper.toEntity(statusCategoryRepository.save(statusCategoryMapper.toModel(statusCategory)));
    }

    @Override
    public void updateStatusCategory(StatusCategoryEntity statusCategory) {
        statusCategoryRepository.save(statusCategoryMapper.toModel(statusCategory));
    }

    @Override
    public boolean existsByKey(Long tenantId, String key) {
        return statusCategoryRepository.existsByTenantIdAndKeyIgnoreCase(tenantId, key);
    }

    private Sort resolveSort(StatusCategoryListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "key" -> Sort.by(
                    new Sort.Order(direction, "key"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of name, key, created_at");
        };
    }
}
