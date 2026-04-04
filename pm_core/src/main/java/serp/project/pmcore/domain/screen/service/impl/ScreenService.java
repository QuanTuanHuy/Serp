package serp.project.pmcore.domain.screen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.screen.entity.ScreenEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.domain.screen.port.IScreenTabPort;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenService implements IScreenService {

    private final IScreenPort screenPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;

    @Override
    public List<ScreenTabFieldEntity> getScreenTabFieldsByScreenId(Long screenId, Long tenantId) {
        getScreenById(screenId, tenantId);

        List< ScreenTabEntity> tabs = screenTabPort.getScreenTabsByScreenId(screenId, tenantId);
        if (tabs.isEmpty()) {
            log.debug("[ScreenService] No tabs found for screen: id={}, tenantId={}", screenId, tenantId);
            return Collections.emptyList();
        }
        List<Long> tabIds = tabs.stream().map(ScreenTabEntity::getId).toList();
        return screenTabFieldPort.getScreenTabFieldsByScreenTabIds(tabIds, tenantId);
    }

    @Override
    public ScreenEntity getScreenById(Long screenId, Long tenantId) {
        return screenPort.getScreenById(screenId, tenantId).orElseThrow(() -> {
            log.warn("[ScreenService] Screen not found: id={}, tenantId={}", screenId, tenantId);
            return new ResourceNotFoundException(
                    DomainErrorCode.SCREEN_NOT_FOUND,
                    String.format("Screen not found: id=%d, tenantId=%d", screenId, tenantId)
            );
        });
    }

}
