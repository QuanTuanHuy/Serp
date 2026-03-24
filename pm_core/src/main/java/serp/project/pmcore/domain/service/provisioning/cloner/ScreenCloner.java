package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenEntity;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.exception.*;
import serp.project.pmcore.domain.port.store.IScreenPort;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.domain.port.store.IScreenTabPort;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScreenCloner {

    private final IScreenPort screenPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;

    public Long cloneScreenBySourceId(Long sourceScreenId,
                                      Long tenantId,
                                      Long userId,
                                      CloneMode cloneMode) {
        Objects.requireNonNull(sourceScreenId, "sourceScreenId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");

        ScreenEntity source = screenPort
                .getScreenByIdIncludingSystem(sourceScreenId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Screen not found for source id=" + sourceScreenId
                ));

        return cloneScreen(source, tenantId, userId, cloneMode);
    }

    public Long cloneScreen(ScreenEntity source,
                            Long tenantId,
                            Long userId,
                            CloneMode cloneMode) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");

        List<ScreenTabEntity> sourceTabs = screenTabPort
                .getScreenTabsByScreenIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();

        ScreenEntity cloned = ScreenEntity.builder()
                .tenantId(tenantId)
                .name(source.getName())
                .description(source.getDescription())
                .build();
        cloned.applyCreate(userId, now);
        ScreenEntity saved = screenPort.createScreen(cloned);

        Map<Long, Long> tabIdMap = new HashMap<>();

        if (!sourceTabs.isEmpty()) {
            List<ScreenTabEntity> clonedTabs = new ArrayList<>();
            for (ScreenTabEntity tab : sourceTabs) {
                clonedTabs.add(ScreenTabEntity.builder()
                        .tenantId(tenantId)
                        .screenId(saved.getId())
                        .name(tab.getName())
                        .sequence(tab.getSequence())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }

            List<ScreenTabEntity> savedTabs = screenTabPort.createScreenTabs(clonedTabs);
            for (int i = 0; i < sourceTabs.size(); i++) {
                tabIdMap.put(sourceTabs.get(i).getId(), savedTabs.get(i).getId());
            }

            List<ScreenTabFieldEntity> clonedFields = new ArrayList<>();
            for (ScreenTabEntity sourceTab : sourceTabs) {
                List<ScreenTabFieldEntity> sourceFields = screenTabFieldPort
                        .getScreenTabFieldsByScreenTabIdIncludingSystem(sourceTab.getId(), tenantId);

                for (ScreenTabFieldEntity sourceField : sourceFields) {
                    clonedFields.add(ScreenTabFieldEntity.builder()
                            .tenantId(tenantId)
                            .screenTabId(requireMappedId(tabIdMap, sourceTab.getId()))
                            .fieldRefType(sourceField.getFieldRefType())
                            .fieldRef(sourceField.getFieldRef())
                            .sequence(sourceField.getSequence())
                            .build());
                }
            }

            if (!clonedFields.isEmpty()) {
                screenTabFieldPort.createScreenTabFields(clonedFields);
            }
        }

        log.info("Created {} SCREEN clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing " + "screen tab" + " mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }
}