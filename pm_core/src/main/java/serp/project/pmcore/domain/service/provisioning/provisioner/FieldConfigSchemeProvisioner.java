package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.cloner.FieldConfigCloner;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FieldConfigSchemeProvisioner extends AbstractMappedSharedProvisioner<FieldConfigSchemeEntity> {

    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IssueTypeMaterializer issueTypeMaterializer;
    private final FieldConfigCloner fieldConfigCloner;
    private final CloneNamingHelper cloneNamingHelper;

    public FieldConfigSchemeProvisioner(ITenantSchemeMappingPort tenantSchemeMappingPort,
                                        IFieldConfigSchemePort fieldConfigSchemePort,
                                        IFieldConfigSchemeItemPort fieldConfigSchemeItemPort,
                                        IssueTypeMaterializer issueTypeMaterializer,
                                        FieldConfigCloner fieldConfigCloner,
                                        CloneNamingHelper cloneNamingHelper) {
        super(tenantSchemeMappingPort);
        this.fieldConfigSchemePort = fieldConfigSchemePort;
        this.fieldConfigSchemeItemPort = fieldConfigSchemeItemPort;
        this.issueTypeMaterializer = issueTypeMaterializer;
        this.fieldConfigCloner = fieldConfigCloner;
        this.cloneNamingHelper = cloneNamingHelper;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.FIELD_CONFIG;
    }

    @Override
    protected Optional<FieldConfigSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return fieldConfigSchemePort.getFieldConfigSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(FieldConfigSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(FieldConfigSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return fieldConfigSchemePort.getFieldConfigSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "field config scheme";
    }

    @Override
    protected Long cloneForTenant(FieldConfigSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode cloneMode,
                                  ProvisioningExecutionContext context) {
        List<FieldConfigSchemeItemEntity> sourceItems = fieldConfigSchemeItemPort
                .getFieldConfigSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = materializeIssueTypes(sourceItems, tenantId, userId);
        Map<Long, Long> fieldConfigIdMap = cloneFieldConfigs(source, sourceItems, tenantId, userId, cloneMode);

        long now = System.currentTimeMillis();

        FieldConfigSchemeEntity cloned = FieldConfigSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName("", source.getName(), SchemeType.FIELD_CONFIG, cloneMode))
                .description(source.getDescription())
                .defaultFieldConfigId(requireMappedId(
                        fieldConfigIdMap,
                        source.getDefaultFieldConfigId(),
                        "field configuration"
                ))
                .build();
        cloned.applyCreate(userId, now);
        FieldConfigSchemeEntity saved = fieldConfigSchemePort.createFieldConfigScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<FieldConfigSchemeItemEntity> clonedItems = new ArrayList<>();
            for (FieldConfigSchemeItemEntity item : sourceItems) {
                clonedItems.add(FieldConfigSchemeItemEntity.builder()
                        .tenantId(tenantId)
                        .schemeId(saved.getId())
                        .issueTypeId(requireMappedId(issueTypeIdMap, item.getIssueTypeId(), "issue type"))
                        .fieldConfigId(requireMappedId(fieldConfigIdMap, item.getFieldConfigId(), "field configuration"))
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }
            fieldConfigSchemeItemPort.createFieldConfigSchemeItems(clonedItems);
        }

        log.info("Created {} FIELD_CONFIG scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private Map<Long, Long> materializeIssueTypes(List<FieldConfigSchemeItemEntity> sourceItems,
                                                  Long tenantId,
                                                  Long userId) {
        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> sourceIssueTypeIds = sourceItems.stream()
                .map(FieldConfigSchemeItemEntity::getIssueTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Long sourceIssueTypeId : sourceIssueTypeIds) {
            issueTypeIdMap.put(
                    sourceIssueTypeId,
                    issueTypeMaterializer.materialize(sourceIssueTypeId, tenantId, userId)
            );
        }

        return issueTypeIdMap;
    }

    private Map<Long, Long> cloneFieldConfigs(FieldConfigSchemeEntity source,
                                              List<FieldConfigSchemeItemEntity> sourceItems,
                                              Long tenantId,
                                              Long userId,
                                              CloneMode cloneMode) {
        Map<Long, Long> fieldConfigIdMap = new HashMap<>();
        Set<Long> sourceFieldConfigIds = new HashSet<>();

        for (FieldConfigSchemeItemEntity item : sourceItems) {
            if (item.getFieldConfigId() != null) {
                sourceFieldConfigIds.add(item.getFieldConfigId());
            }
        }

        if (source.getDefaultFieldConfigId() != null) {
            sourceFieldConfigIds.add(source.getDefaultFieldConfigId());
        }

        for (Long sourceFieldConfigId : sourceFieldConfigIds) {
            fieldConfigIdMap.put(
                    sourceFieldConfigId,
                    fieldConfigCloner.cloneFieldConfigBySourceId(sourceFieldConfigId, tenantId, userId, cloneMode)
            );
        }

        return fieldConfigIdMap;
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId, String entityName) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing " + entityName + " mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }
}
