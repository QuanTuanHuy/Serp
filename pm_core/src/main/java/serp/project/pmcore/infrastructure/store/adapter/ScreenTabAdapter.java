/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.domain.port.store.IScreenTabPort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenTabMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenTabRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScreenTabAdapter implements IScreenTabPort {

    private final IScreenTabRepository screenTabRepository;
    private final ScreenTabMapper screenTabMapper;

    @Override
    public List<ScreenTabEntity> createScreenTabs(List<ScreenTabEntity> tabs) {
        if (tabs == null || tabs.isEmpty()) {
            return new ArrayList<>();
        }
        return screenTabMapper.toEntities(screenTabRepository.saveAll(screenTabMapper.toModels(tabs)));
    }

    @Override
    public List<ScreenTabEntity> getScreenTabsByScreenIdIncludingSystem(Long screenId, Long tenantId) {
        return screenTabMapper.toEntities(
                screenTabRepository.findAllByScreenIdAndTenantIdOrSystemTenant(screenId, tenantId)
        );
    }
}
