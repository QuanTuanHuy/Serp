/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.port.store.IScreenSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScreenSchemeItemAdapter implements IScreenSchemeItemPort {

    private final IScreenSchemeItemRepository screenSchemeItemRepository;
    private final ScreenSchemeItemMapper screenSchemeItemMapper;

    @Override
    public List<ScreenSchemeItemEntity> createScreenSchemeItems(List<ScreenSchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return screenSchemeItemMapper.toEntities(screenSchemeItemRepository.saveAll(screenSchemeItemMapper.toModels(items)));
    }

    @Override
    public List<ScreenSchemeItemEntity> getScreenSchemeItemsByScreenSchemeIdIncludingSystem(Long screenSchemeId, Long tenantId) {
        return screenSchemeItemMapper.toEntities(
                screenSchemeItemRepository.findAllByScreenSchemeIdAndTenantIdOrSystemTenant(screenSchemeId, tenantId)
        );
    }
}
