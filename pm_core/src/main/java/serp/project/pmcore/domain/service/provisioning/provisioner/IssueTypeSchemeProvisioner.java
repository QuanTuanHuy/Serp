package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issyetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
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

@Service
@Slf4j
public class IssueTypeSchemeProvisioner extends AbstractMappedSharedProvisioner<IssueTypeSchemeEntity> {

    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IssueTypeMaterializer issueTypeMaterializer;
    private final CloneNamingHelper cloneNamingHelper;

    public IssueTypeSchemeProvisioner(IIssueTypeSchemePort issueTypeSchemePort,
                                      IIssueTypeSchemeItemPort issueTypeSchemeItemPort,
                                      IssueTypeMaterializer issueTypeMaterializer,
                                      CloneNamingHelper cloneNamingHelper,
                                      ITenantSchemeMappingPort tenantSchemeMappingPort) {
        super(tenantSchemeMappingPort);
        this.issueTypeSchemePort = issueTypeSchemePort;
        this.issueTypeSchemeItemPort = issueTypeSchemeItemPort;
        this.issueTypeMaterializer = issueTypeMaterializer;
        this.cloneNamingHelper = cloneNamingHelper;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.ISSUE_TYPE;
    }

    @Override
    protected Long cloneForTenant(IssueTypeSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode mode,
                                  ProvisioningExecutionContext context) {
        List<IssueTypeSchemeItemEntity> sourceItems = issueTypeSchemeItemPort
                .getIssueTypeSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> sourceIssueTypeIds = new HashSet<>();

        sourceItems.stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .filter(Objects::nonNull)
                .forEach(sourceIssueTypeIds::add);
        if (source.getDefaultIssueTypeId() != null) {
            sourceIssueTypeIds.add(source.getDefaultIssueTypeId());
        }

        for (Long sourceIssueTypeId : sourceIssueTypeIds) {
            issueTypeIdMap.put(
                    sourceIssueTypeId,
                    issueTypeMaterializer.materialize(sourceIssueTypeId, tenantId, userId)
            );
        }

        long now = System.currentTimeMillis();
        IssueTypeSchemeEntity cloned = IssueTypeSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.ISSUE_TYPE, mode))
                .description(source.getDescription())
                .defaultIssueTypeId(requiredMappedId(issueTypeIdMap, source.getDefaultIssueTypeId()))
                .build();
        cloned.applyCreate(userId, now);
        IssueTypeSchemeEntity saved = issueTypeSchemePort.createIssueTypeScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<IssueTypeSchemeItemEntity> clonedItems = sourceItems.stream()
                    .map(item -> IssueTypeSchemeItemEntity.builder()
                            .tenantId(tenantId)
                            .schemeId(saved.getId())
                            .issueTypeId(requiredMappedId(issueTypeIdMap, item.getIssueTypeId()))
                            .sequence(item.getSequence())
                            .createdAt(now)
                            .createdBy(userId)
                            .build())
                    .collect(Collectors.toList());
            issueTypeSchemeItemPort.createIssueTypeSchemeItems(clonedItems);

            log.info("Created {} ISSUE_TYPE scheme clone: source={} -> cloned={} (tenantId={})",
                    mode, source.getId(), saved.getId(), tenantId);
        }
        return saved.getId();
    }

    @Override
    protected Optional<IssueTypeSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(IssueTypeSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(IssueTypeSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return issueTypeSchemePort.getIssueTypeSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "issue type scheme";
    }

    private Long requiredMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing issue type mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }

}
