package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainException;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.materializer.PriorityMaterializer;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

import java.util.*;

@Service
@Slf4j
public class PrioritySchemeProvisioner extends AbstractMappedSharedProvisioner<PrioritySchemeEntity> {

    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final PriorityMaterializer priorityMaterializer;
    private final CloneNamingHelper cloneNamingHelper;

    public PrioritySchemeProvisioner(IPrioritySchemePort prioritySchemePort,
                                     IPrioritySchemeItemPort prioritySchemeItemPort,
                                     CloneNamingHelper cloneNamingHelper,
                                     PriorityMaterializer priorityMaterializer,
                                     ITenantSchemeMappingPort tenantSchemeMappingPort) {
        super(tenantSchemeMappingPort);
        this.prioritySchemePort = prioritySchemePort;
        this.prioritySchemeItemPort = prioritySchemeItemPort;
        this.priorityMaterializer = priorityMaterializer;
        this.cloneNamingHelper = cloneNamingHelper;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.PRIORITY;
    }

    @Override
    protected Long cloneForTenant(PrioritySchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode mode,
                                  ProvisioningExecutionContext context) {
        List<PrioritySchemeItemEntity> sourceItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> priorityMap = new HashMap<>();
        Set<Long> sourcePriorityIds = new HashSet<>();

        sourceItems.stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .filter(Objects::nonNull)
                .forEach(sourcePriorityIds::add);
        if (source.getDefaultPriorityId() != null) {
            sourcePriorityIds.add(source.getDefaultPriorityId());
        }

        for (Long sourcePriorityId : sourcePriorityIds) {
            priorityMap.put(
                    sourcePriorityId,
                    priorityMaterializer.materialize(sourcePriorityId, tenantId, userId)
            );
        }

        long now = System.currentTimeMillis();
        PrioritySchemeEntity schemeCloned = PrioritySchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.PRIORITY, mode))
                .description(source.getDescription())
                .defaultPriorityId(requiredMappedId(priorityMap, source.getDefaultPriorityId()))
                .build();
        schemeCloned.applyCreate(userId, now);
        PrioritySchemeEntity saved = prioritySchemePort.createPriorityScheme(schemeCloned);

        if (!sourceItems.isEmpty()) {
            List<PrioritySchemeItemEntity> clonedItems = sourceItems.stream()
                    .map(item -> {
                        PrioritySchemeItemEntity cloned = PrioritySchemeItemEntity.builder()
                                .tenantId(tenantId)
                                .schemeId(saved.getId())
                                .priorityId(requiredMappedId(priorityMap, item.getPriorityId()))
                                .sequence(item.getSequence())
                                .build();
                        cloned.applyCreate(userId, now);
                        return cloned;
                    })
                    .toList();
            prioritySchemeItemPort.createPrioritySchemeItems(clonedItems);

            log.info("Created {} PRIORITY scheme clone: source={} -> cloned={} (tenantId={})",
                    mode, source.getId(), saved.getId(), tenantId);
        }

        return saved.getId();

    }

    @Override
    protected Optional<PrioritySchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(PrioritySchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(PrioritySchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "priority scheme";
    }

    private Long requiredMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing priority mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }
}
