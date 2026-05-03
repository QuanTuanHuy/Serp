/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.manageitems;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeDetailView;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeIssueTypeView;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeWorkflowView;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManageWorkflowSchemeItemsCommandHandler implements ICommandHandler<ManageWorkflowSchemeItemsCommand, WorkflowSchemeDetailView> {

    private final IWorkflowSchemeService workflowSchemeService;
    private final IIssueTypeService issueTypeService;
    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowSchemeDetailView handle(ManageWorkflowSchemeItemsCommand command) {
        WorkflowSchemeEntity updated = workflowSchemeService.replaceWorkflowSchemeItems(
                command.schemeId(),
                command.items().stream()
                        .map(item -> new IWorkflowSchemeService.WorkflowSchemeItemReplacement(
                                item.issueTypeId(),
                                item.workflowId()
                        ))
                        .toList(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowSchemeDetailView.from(
                updated,
                buildIssueTypeMap(updated, command.tenantId()),
                buildWorkflowMap(updated, command.tenantId())
        );
    }

    private Map<Long, WorkflowSchemeIssueTypeView> buildIssueTypeMap(WorkflowSchemeEntity scheme, Long tenantId) {
        Map<Long, WorkflowSchemeIssueTypeView> issueTypesById = new LinkedHashMap<>();
        if (scheme.getItems() == null) {
            return issueTypesById;
        }

        List<Long> issueTypeIds = scheme.getItems().stream()
                .map(WorkflowSchemeItemEntity::getIssueTypeId)
                .distinct()
                .toList();
        issueTypeService.getVisibleIssueTypesByIds(issueTypeIds, tenantId)
                .forEach(issueType -> issueTypesById.put(issueType.getId(), WorkflowSchemeIssueTypeView.from(issueType)));
        return issueTypesById;
    }

    private Map<Long, WorkflowSchemeWorkflowView> buildWorkflowMap(WorkflowSchemeEntity scheme, Long tenantId) {
        Map<Long, WorkflowSchemeWorkflowView> workflowsById = new LinkedHashMap<>();
        if (scheme.getItems() == null) {
            return workflowsById;
        }

        List<Long> workflowIds = scheme.getItems().stream()
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .distinct()
                .toList();
        for (Long workflowId : workflowIds) {
            WorkflowEntity workflow = workflowService.getVisibleWorkflowById(workflowId, tenantId);
            workflowsById.put(workflow.getId(), WorkflowSchemeWorkflowView.from(workflow));
        }
        return workflowsById;
    }
}
