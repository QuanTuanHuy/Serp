package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainException;
import serp.project.pmcore.domain.port.store.*;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.cloner.WorkflowCloner;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

import java.util.*;

@Component
@Slf4j
public class WorkflowSchemeProvisioner extends AbstractMappedSharedProvisioner<WorkflowSchemeEntity> {

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;

    private final WorkflowCloner workflowCloner;
    private final IssueTypeMaterializer issueTypeMaterializer;
    private final CloneNamingHelper cloneNamingHelper;

    public WorkflowSchemeProvisioner(IWorkflowSchemePort workflowSchemePort,
                                     IWorkflowSchemeItemPort workflowSchemeItemPort,
                                     WorkflowCloner workflowCloner,
                                     IssueTypeMaterializer issueTypeMaterializer,
                                     CloneNamingHelper cloneNamingHelper,
                                     ITenantSchemeMappingPort tenantSchemeMappingPort) {
        super(tenantSchemeMappingPort);
        this.workflowSchemePort = workflowSchemePort;
        this.workflowSchemeItemPort = workflowSchemeItemPort;
        this.workflowCloner = workflowCloner;
        this.issueTypeMaterializer = issueTypeMaterializer;
        this.cloneNamingHelper = cloneNamingHelper;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.WORKFLOW;
    }

    @Override
    protected Long cloneForTenant(WorkflowSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode mode,
                                  ProvisioningExecutionContext context) {
        List<WorkflowSchemeItemEntity> sourceItems = workflowSchemeItemPort
                .getWorkflowSchemeItemsBySchemeIdIncludingSystem(source.getId(), tenantId);

        Map<Long, Long> issueTypeIdMap = materializeIssueTypes(sourceItems, tenantId, userId);
        Map<Long, Long> workflowIdMap = materializeWorkflows(source, sourceItems, tenantId, userId, mode, context);

        long now = System.currentTimeMillis();
        WorkflowSchemeEntity cloned = WorkflowSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.WORKFLOW, mode))
                .description(source.getDescription())
                .defaultWorkflowId(requireMappedId(workflowIdMap, source.getDefaultWorkflowId(), "workflow"))
                .build();
        cloned.applyCreate(userId, now);
        WorkflowSchemeEntity saved = workflowSchemePort.createWorkflowScheme(cloned);

        if (!sourceItems.isEmpty()) {
            List<WorkflowSchemeItemEntity> clonedItems = new ArrayList<>();
            for (WorkflowSchemeItemEntity sourceItem : sourceItems) {
                WorkflowSchemeItemEntity clonedItem = WorkflowSchemeItemEntity.builder()
                        .tenantId(tenantId)
                        .schemeId(saved.getId())
                        .issueTypeId(requireMappedId(issueTypeIdMap, sourceItem.getIssueTypeId(), "issue type"))
                        .workflowId(requireMappedId(workflowIdMap, sourceItem.getWorkflowId(), "workflow"))
                        .build();
                clonedItem.applyCreate(userId, now);
                clonedItems.add(clonedItem);
            }
            workflowSchemeItemPort.createWorkflowSchemeItems(clonedItems);
        }

        log.info("Created {} WORKFLOW scheme clone: source={} -> cloned={} (tenantId={})",
                mode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private Map<Long, Long> materializeIssueTypes(List<WorkflowSchemeItemEntity> sourceItems,
                                                  Long tenantId,
                                                  Long userId) {
        Map<Long, Long> issueTypeIdMap = new HashMap<>();
        Set<Long> sourceIssueTypeIds = new HashSet<>();

        for (WorkflowSchemeItemEntity sourceItem : sourceItems) {
            if (sourceItem.getIssueTypeId() != null) {
                sourceIssueTypeIds.add(sourceItem.getIssueTypeId());
            }
        }
        for (Long sourceIssueTypeId : sourceIssueTypeIds) {
            issueTypeIdMap.put(
                    sourceIssueTypeId,
                    issueTypeMaterializer.materialize(sourceIssueTypeId, tenantId, userId)
            );
        }
        return issueTypeIdMap;
    }

    private Map<Long, Long> materializeWorkflows(WorkflowSchemeEntity source,
                                                 List<WorkflowSchemeItemEntity> sourceItems,
                                                 Long tenantId,
                                                 Long userId,
                                                 CloneMode mode,
                                                 ProvisioningExecutionContext context) {
        Map<Long, Long> workflowIdMap = new HashMap<>();
        Set<Long> sourceWorkflowIds = new HashSet<>();

        for (WorkflowSchemeItemEntity sourceItem : sourceItems) {
            if (sourceItem.getWorkflowId() != null) {
                sourceWorkflowIds.add(sourceItem.getWorkflowId());
            }
        }
        if (source.getDefaultWorkflowId() != null) {
            sourceWorkflowIds.add(source.getDefaultWorkflowId());
        }

        for (Long sourceWorkflowId : sourceWorkflowIds) {
            workflowIdMap.put(
                    sourceWorkflowId,
                    workflowCloner.cloneWorkflowBySourceId(sourceWorkflowId, tenantId, userId, mode, context)
            );
        }

        return workflowIdMap;
    }

    @Override
    protected Optional<WorkflowSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(WorkflowSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(WorkflowSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return workflowSchemePort.getWorkflowSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "workflow scheme";
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
