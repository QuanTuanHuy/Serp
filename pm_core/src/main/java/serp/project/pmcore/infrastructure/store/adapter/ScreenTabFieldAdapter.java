/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.infrastructure.store.mapper.ScreenTabFieldMapper;
import serp.project.pmcore.infrastructure.store.repository.IScreenTabFieldRepository;

import java.util.ArrayList;
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
}
