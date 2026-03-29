/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScreenSchemeAdapter implements IScreenSchemePort {

    private final IScreenSchemeRepository screenSchemeRepository;
    private final ScreenSchemeMapper screenSchemeMapper;

    @Override
    public ScreenSchemeEntity createScreenScheme(ScreenSchemeEntity scheme) {
        return screenSchemeMapper.toEntity(screenSchemeRepository.save(screenSchemeMapper.toModel(scheme)));
    }

    @Override
    public Optional<ScreenSchemeEntity> getScreenSchemeById(Long schemeId, Long tenantId) {
        return screenSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(screenSchemeMapper::toEntity);
    }

    @Override
    public Optional<ScreenSchemeEntity> getScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return screenSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(screenSchemeMapper::toEntity);
    }
}
