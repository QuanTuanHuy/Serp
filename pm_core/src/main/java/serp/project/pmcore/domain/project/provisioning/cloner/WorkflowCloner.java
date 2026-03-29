package serp.project.pmcore.domain.project.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowCloner {

    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;

    private final WorkflowVersionTreeCloner workflowVersionTreeCloner;
    private final CloneNamingHelper cloneNamingHelper;

    public Long cloneWorkflowBySourceId(Long sourceWorkflowId,
                                        Long tenantId,
                                        Long userId,
                                        CloneMode cloneMode,
                                        ProvisioningExecutionContext context) {
        validateRequired(sourceWorkflowId, "sourceWorkflowId");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        WorkflowEntity source = workflowPort
                .getWorkflowByIdIncludingSystem(sourceWorkflowId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_NOT_FOUND,
                        "Workflow not found for source id=" + sourceWorkflowId
                ));
        return cloneWorkflow(source, tenantId, userId, cloneMode, context);
    }

    public Long cloneWorkflow(WorkflowEntity source,
                              Long tenantId,
                              Long userId,
                              CloneMode cloneMode,
                              ProvisioningExecutionContext context) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        if (source.getCurrentPublishedVersionId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_WORKFLOW_FAILED,
                    "Workflow root must have a current published version before provisioning: workflowId=" + source.getId()
            );
        }

        List<WorkflowVersionEntity> sourceVersions = workflowVersionPort
                .getWorkflowVersionsByWorkflowIdIncludingSystem(source.getId(), tenantId);
        if (sourceVersions.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_WORKFLOW_FAILED,
                    "Workflow root has no version history to provision: workflowId=" + source.getId()
            );
        }

        long now = System.currentTimeMillis();
        WorkflowEntity cloned = WorkflowEntity.builder()
                .tenantId(tenantId)
                .workflowKey(cloneNamingHelper.buildWorkflowCloneKey(
                        context.getProjectKey(), source.getWorkflowKey(), source.getName(), source.getId(), cloneMode, true))
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.WORKFLOW, cloneMode))
                .description(source.getDescription())
                .currentPublishedVersionId(null)
                .draftVersionId(null)
                .lifecycleState(source.getLifecycleState() == null ? WorkflowLifecycleState.ACTIVE : source.getLifecycleState())
                .isSystem(false)
                .build();
        cloned.applyCreate(userId, now);
        WorkflowEntity savedWorkflow = workflowPort.createWorkflow(cloned);

        Map<Long, Long> versionIdMap = new HashMap<>();
        List<WorkflowVersionEntity> clonedVersions = new ArrayList<>();
        for (WorkflowVersionEntity sourceVersion : sourceVersions) {
            clonedVersions.add(WorkflowVersionEntity.builder()
                            .tenantId(tenantId)
                            .workflowId(savedWorkflow.getId())
                            .versionNo(sourceVersion.getVersionNo())
                            .versionState(sourceVersion.getVersionState())
                            .baseVersionId(null)
                            .publishedAt(sourceVersion.getPublishedAt())
                            .publishedBy(sourceVersion.getPublishedBy())
                            .createdAt(now)
                            .createdBy(userId)
                    .build());
        }

        List<WorkflowVersionEntity> savedVersions = workflowVersionPort.createWorkflowVersions(clonedVersions);
        for (int i = 0; i < sourceVersions.size(); i++) {
            versionIdMap.put(sourceVersions.get(i).getId(), savedVersions.get(i).getId());
        }

        for (int i = 0; i < sourceVersions.size(); i++) {
            WorkflowVersionEntity sourceVersion = sourceVersions.get(i);
            WorkflowVersionEntity savedVersion = savedVersions.get(i);

            savedVersion.setBaseVersionId(
                    requireMappedId(versionIdMap, sourceVersion.getBaseVersionId())
            );
            workflowVersionPort.updateWorkflowVersion(savedVersion);

            workflowVersionTreeCloner.cloneVersionTree(
                    sourceVersion,
                    savedVersion.getId(),
                    tenantId,
                    userId,
                    cloneMode,
                    context
            );
        }

        savedWorkflow.setCurrentPublishedVersionId(
                requireMappedId(versionIdMap, source.getCurrentPublishedVersionId())
        );
        savedWorkflow.setDraftVersionId(
                requireMappedId(versionIdMap, source.getDraftVersionId())
        );
        workflowPort.updateWorkflow(savedWorkflow);

        log.info("Created {} WORKFLOW clone: source={} -> target={} (tenantId={})",
                cloneMode, source.getId(), savedWorkflow.getId(), tenantId);

        return savedWorkflow.getId();
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_WORKFLOW_FAILED,
                    "Missing workflow version mapping for source id=" + sourceId
            );
        }

        return mappedId;
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_WORKFLOW_FAILED,
                    fieldName + " is required"
            );
        }
    }
}
