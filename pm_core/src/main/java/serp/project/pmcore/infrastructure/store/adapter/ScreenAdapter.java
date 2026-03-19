/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenEntity;
import serp.project.pmcore.domain.port.store.IScreenPort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScreenAdapter implements IScreenPort {

    private final IScreenRepository screenRepository;
    private final ScreenMapper screenMapper;

    @Override
    public ScreenEntity createScreen(ScreenEntity screen) {
        return screenMapper.toEntity(screenRepository.save(screenMapper.toModel(screen)));
    }

    @Override
    public Optional<ScreenEntity> getScreenById(Long screenId, Long tenantId) {
        return screenRepository.findByIdAndTenantId(screenId, tenantId)
                .map(screenMapper::toEntity);
    }

    @Override
    public Optional<ScreenEntity> getScreenByIdIncludingSystem(Long screenId, Long tenantId) {
        return screenRepository.findByIdAndTenantIdOrSystemTenant(screenId, tenantId)
                .map(screenMapper::toEntity);
    }
}
