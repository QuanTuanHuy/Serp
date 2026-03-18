/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.port.store.IStatusCategoryPort;
import serp.project.pmcore.infrastructure.store.mapper.StatusCategoryMapper;
import serp.project.pmcore.infrastructure.store.repository.IStatusCategoryRepository;

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
    public StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory) {
        return statusCategoryMapper.toEntity(statusCategoryRepository.save(statusCategoryMapper.toModel(statusCategory)));
    }
}
