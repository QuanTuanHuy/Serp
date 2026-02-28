/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.StatusEntity;
import serp.project.pmcore.core.port.store.IStatusPort;
import serp.project.pmcore.infrastructure.store.mapper.StatusMapper;
import serp.project.pmcore.infrastructure.store.repository.IStatusRepository;

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
}
