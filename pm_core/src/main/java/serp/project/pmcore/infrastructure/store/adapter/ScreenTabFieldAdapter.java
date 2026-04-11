/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenTabFieldMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenTabFieldRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScreenTabFieldAdapter implements IScreenTabFieldPort {

    private final IScreenTabFieldRepository screenTabFieldRepository;
    private final ScreenTabFieldMapper screenTabFieldMapper;

    @Override
    public List<ScreenTabFieldEntity> createScreenTabFields(List<ScreenTabFieldEntity> fields) {
        if (fields == null || fields.isEmpty()) {
            return new ArrayList<>();
        }
        return screenTabFieldMapper.toEntities(screenTabFieldRepository.saveAll(screenTabFieldMapper.toModels(fields)));
    }

    @Override
    public List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabIdIncludingSystem(Long screenTabId, Long tenantId) {
        return screenTabFieldMapper.toEntities(
                screenTabFieldRepository.findAllByScreenTabIdAndTenantIdOrSystemTenant(screenTabId, tenantId)
        );
    }

    @Override
    public List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabId(Long screenTabId, Long tenantId) {
        return screenTabFieldMapper.toEntities(
                screenTabFieldRepository.findAllByScreenTabIdAndTenantId(screenTabId, tenantId)
        );
    }

    @Override
    public List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabIds(Collection<Long> screenTabIds, Long tenantId) {
        return screenTabFieldMapper.toEntities(
                screenTabFieldRepository.findAllByScreenTabIdsAndTenantId(screenTabIds, tenantId)
        );
    }
}
