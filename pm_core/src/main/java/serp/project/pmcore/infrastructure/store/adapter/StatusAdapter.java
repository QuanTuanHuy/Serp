/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.StatusEntity;
import serp.project.pmcore.domain.port.store.IStatusPort;
import serp.project.pmcore.infrastructure.store.mapper.StatusMapper;
import serp.project.pmcore.infrastructure.store.model.StatusModel;
import serp.project.pmcore.infrastructure.store.repository.IStatusRepository;

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
    public StatusEntity createStatus(StatusEntity status) {
        return statusMapper.toEntity(statusRepository.save(statusMapper.toModel(status)));
    }

    @Override
    public List<StatusEntity> getStatusesByTenantId(Long tenantId) {
        return statusMapper.toEntities(
                statusRepository.findAllByTenantId(tenantId));
    }

    @Override
    public List<StatusEntity> createStatuses(List<StatusEntity> statuses) {
        List<StatusModel> models = statusMapper.toModels(statuses);
        return statusMapper.toEntities(statusRepository.saveAll(models));
    }
}
