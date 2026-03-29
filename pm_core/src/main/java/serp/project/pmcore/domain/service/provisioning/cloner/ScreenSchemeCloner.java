package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainException;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScreenSchemeCloner {

    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final ScreenCloner screenCloner;
    private final CloneNamingHelper cloneNamingHelper;

    public Long cloneScreenSchemeBySourceId(Long sourceScreenSchemeId,
                                            Long tenantId,
                                            Long userId,
                                            CloneMode cloneMode,
                                            ProvisioningExecutionContext context) {
        validateRequired(sourceScreenSchemeId, "sourceScreenSchemeId");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        ScreenSchemeEntity source = screenSchemePort
                .getScreenSchemeByIdIncludingSystem(sourceScreenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found for source id=" + sourceScreenSchemeId
                ));
        return cloneScreenScheme(source, tenantId, userId, cloneMode, context);
    }

    public Long cloneScreenScheme(ScreenSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode cloneMode,
                                  ProvisioningExecutionContext context) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<ScreenSchemeItemEntity> sourceItems = screenSchemeItemPort
                .getScreenSchemeItemsByScreenSchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> screenIdMap = new HashMap<>();
        for (Long sourceScreenId : collectSourceScreenIds(source, sourceItems)) {
            screenIdMap.put(
                    sourceScreenId,
                    screenCloner.cloneScreenBySourceId(sourceScreenId, tenantId, userId, cloneMode, context)
            );
        }

        long now = System.currentTimeMillis();
        ScreenSchemeEntity cloned = ScreenSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.SCREEN, cloneMode))
                .description(source.getDescription())
                .defaultScreenId(requireMappedId(screenIdMap, source.getDefaultScreenId()))
                .build();
        cloned.applyCreate(userId, now);
        ScreenSchemeEntity saved = screenSchemePort.createScreenScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<ScreenSchemeItemEntity> clonedItems = new ArrayList<>();
            for (ScreenSchemeItemEntity sourceItem : sourceItems) {
                clonedItems.add(ScreenSchemeItemEntity.builder()
                                .tenantId(tenantId)
                                .screenSchemeId(saved.getId())
                                .operationKey(sourceItem.getOperationKey())
                                .screenId(requireMappedId(screenIdMap, sourceItem.getScreenId()))
                                .createdAt(now)
                                .createdBy(userId)
                        .build());
            }
            screenSchemeItemPort.createScreenSchemeItems(clonedItems);
        }

        log.info("Created {} SCREEN scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);
        return saved.getId();
    }

    private List<Long> collectSourceScreenIds(ScreenSchemeEntity source,
                                              List<ScreenSchemeItemEntity> sourceItems) {
        Set<Long> screenIds = new HashSet<>();
        if (source.getDefaultScreenId() != null) {
            screenIds.add(source.getDefaultScreenId());
        }
        for (ScreenSchemeItemEntity item : sourceItems) {
            if (item.getScreenId() != null) {
                screenIds.add(item.getScreenId());
            }
        }
        return new ArrayList<>(screenIds);
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.CLONE_SCREEN_SCHEME_FAILED,
                    "Missing screen mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_SCREEN_SCHEME_FAILED,
                    fieldName + " is required"
            );
        }
    }
}
