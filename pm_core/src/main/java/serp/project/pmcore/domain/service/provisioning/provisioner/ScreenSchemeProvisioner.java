package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.cloner.ScreenSchemeCloner;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainException;
import serp.project.pmcore.domain.shared.port.store.ITenantSchemeMappingPort;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScreenSchemeProvisioner extends AbstractMappedSharedProvisioner<IssueTypeScreenSchemeEntity> {

    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;

    private final ScreenSchemeCloner screenSchemeCloner;
    private final IssueTypeMaterializer issueTypeMaterializer;
    private final CloneNamingHelper cloneNamingHelper;

    public ScreenSchemeProvisioner(IIssueTypeScreenSchemePort issueTypeScreenSchemePort,
                                   IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort,
                                   ScreenSchemeCloner screenSchemeCloner,
                                   IssueTypeMaterializer issueTypeMaterializer,
                                   CloneNamingHelper cloneNamingHelper,
                                   ITenantSchemeMappingPort tenantSchemeMappingPort) {
        super(tenantSchemeMappingPort);
        this.issueTypeScreenSchemePort = issueTypeScreenSchemePort;
        this.issueTypeScreenSchemeItemPort = issueTypeScreenSchemeItemPort;
        this.screenSchemeCloner = screenSchemeCloner;
        this.issueTypeMaterializer = issueTypeMaterializer;
        this.cloneNamingHelper = cloneNamingHelper;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.SCREEN;
    }

    @Override
    protected Long cloneForTenant(IssueTypeScreenSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode mode,
                                  ProvisioningExecutionContext context) {
        List<IssueTypeScreenSchemeItemEntity> sourceItems = issueTypeScreenSchemeItemPort
                .getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = materializeIssueTypes(sourceItems, tenantId, userId);
        Map<Long, Long> screenSchemeIdMap = cloneScreenSchemes(source, sourceItems, tenantId, userId, mode, context);

        long now = System.currentTimeMillis();
        IssueTypeScreenSchemeEntity cloned = IssueTypeScreenSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.SCREEN, mode))
                .description(source.getDescription())
                .defaultScreenSchemeId(requireMappedId(screenSchemeIdMap, source.getDefaultScreenSchemeId(), "screen scheme"))
                .build();
        cloned.applyCreate(userId, now);
        IssueTypeScreenSchemeEntity saved = issueTypeScreenSchemePort.createIssueTypeScreenScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<IssueTypeScreenSchemeItemEntity> clonedItems = new ArrayList<>();
            for (IssueTypeScreenSchemeItemEntity sourceItem : sourceItems) {
                IssueTypeScreenSchemeItemEntity clonedItem = IssueTypeScreenSchemeItemEntity.builder()
                        .tenantId(tenantId)
                        .schemeId(saved.getId())
                        .issueTypeId(requireMappedId(issueTypeIdMap, sourceItem.getIssueTypeId(), "issue type"))
                        .screenSchemeId(requireMappedId(screenSchemeIdMap, sourceItem.getScreenSchemeId(), "screen scheme"))
                        .build();
                clonedItem.applyCreate(userId, now);
                clonedItems.add(clonedItem);
            }
            issueTypeScreenSchemeItemPort.createIssueTypeScreenSchemeItems(clonedItems);
        }

        log.info("Created {} SCREEN scheme clone: source={} -> cloned={} (tenantId={})",
                mode, source.getId(), saved.getId(), tenantId);
        return saved.getId();
    }

    private Map<Long, Long> materializeIssueTypes(List<IssueTypeScreenSchemeItemEntity> sourceItems,
                                                  Long tenantId,
                                                  Long userId) {
        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> issueTypeIds = sourceItems.stream()
                .map(IssueTypeScreenSchemeItemEntity::getIssueTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long issueTypeId : issueTypeIds) {
            issueTypeIdMap.put(
                    issueTypeId,
                    issueTypeMaterializer.materialize(issueTypeId, tenantId, userId)
            );
        }

        return issueTypeIdMap;
    }

    private Map<Long, Long> cloneScreenSchemes(IssueTypeScreenSchemeEntity source,
                                               List<IssueTypeScreenSchemeItemEntity> sourceItems,
                                               Long tenantId,
                                               Long userId,
                                               CloneMode mode,
                                               ProvisioningExecutionContext context) {
        Map<Long, Long> screenSchemeIdMap = new HashMap<>();
        Set<Long> sourceScreenSchemeIds = new HashSet<>();

        for (IssueTypeScreenSchemeItemEntity sourceItem : sourceItems) {
            if (sourceItem.getScreenSchemeId() != null) {
                sourceScreenSchemeIds.add(sourceItem.getScreenSchemeId());
            }
        }
        if (source.getDefaultScreenSchemeId() != null) {
            sourceScreenSchemeIds.add(source.getDefaultScreenSchemeId());
        }

        for (Long sourceScreenSchemeId : sourceScreenSchemeIds) {
            screenSchemeIdMap.put(
                    sourceScreenSchemeId,
                    screenSchemeCloner.cloneScreenSchemeBySourceId(sourceScreenSchemeId, tenantId, userId, mode, context)
            );
        }

        return screenSchemeIdMap;
    }

    @Override
    protected Optional<IssueTypeScreenSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return issueTypeScreenSchemePort.getIssueTypeScreenSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(IssueTypeScreenSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(IssueTypeScreenSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "issue type screen scheme";
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId, String entityName) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing " + entityName + " mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }

}
