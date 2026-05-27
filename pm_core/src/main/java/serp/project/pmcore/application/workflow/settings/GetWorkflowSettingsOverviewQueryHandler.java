/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWorkflowSettingsOverviewQueryHandler
        implements IQueryHandler<GetWorkflowSettingsOverviewQuery, WorkflowSettingsOverviewView> {

    private static final int SETTINGS_OVERVIEW_LIMIT = 100;

    private final IWorkflowService workflowService;
    private final IWorkflowSchemeService workflowSchemeService;
    private final IIssueTypeService issueTypeService;
    private final IProjectReadPort projectReadPort;

    @Override
    @Transactional(readOnly = true)
    public WorkflowSettingsOverviewView handle(GetWorkflowSettingsOverviewQuery query) {
        List<WorkflowEntity> workflows = listVisibleWorkflows(query.tenantId());
        List<WorkflowSchemeEntity> schemes = listVisibleWorkflowSchemes(query.tenantId());
        List<WorkflowSchemeEntity> schemeDetails = schemes.stream()
                .map(scheme -> workflowSchemeService.getVisibleWorkflowSchemeDetailById(
                        scheme.getId(),
                        query.tenantId()
                ))
                .toList();

        Map<Long, WorkflowEntity> workflowsById = workflows.stream()
                .collect(Collectors.toMap(WorkflowEntity::getId, Function.identity()));
        Map<Long, IssueTypeEntity> issueTypesById = buildIssueTypesById(query.tenantId(), schemeDetails);
        Map<Long, List<WorkflowSettingsOverviewView.SchemeRefView>> schemeRefsByWorkflowId =
                buildSchemeRefsByWorkflowId(schemeDetails);
        Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> projectRefsBySchemeId =
                buildProjectRefsBySchemeId(query.tenantId(), schemes);
        Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> projectRefsByWorkflowId =
                buildProjectRefsByWorkflowId(schemeDetails, projectRefsBySchemeId);

        return new WorkflowSettingsOverviewView(
                workflows.stream()
                        .map(workflow -> toWorkflowView(
                                workflow,
                                schemeRefsByWorkflowId,
                                projectRefsByWorkflowId
                        ))
                        .toList(),
                schemeDetails.stream()
                        .map(scheme -> toWorkflowSchemeView(
                                scheme,
                                issueTypesById,
                                workflowsById,
                                projectRefsBySchemeId
                        ))
                        .toList()
        );
    }

    private List<WorkflowEntity> listVisibleWorkflows(Long tenantId) {
        WorkflowListCriteria criteria = WorkflowListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .sortBy("name")
                .sortDirection("ASC")
                .build();
        return workflowService.listVisibleWorkflows(tenantId, criteria).items();
    }

    private List<WorkflowSchemeEntity> listVisibleWorkflowSchemes(Long tenantId) {
        WorkflowSchemeListCriteria criteria = WorkflowSchemeListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .sortBy("name")
                .sortDirection("ASC")
                .build();
        return workflowSchemeService.listVisibleWorkflowSchemes(tenantId, criteria).items();
    }

    private Map<Long, IssueTypeEntity> buildIssueTypesById(
            Long tenantId,
            List<WorkflowSchemeEntity> schemes) {
        List<Long> issueTypeIds = schemes.stream()
                .flatMap(scheme -> safeItems(scheme).stream())
                .map(WorkflowSchemeItemEntity::getIssueTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (issueTypeIds.isEmpty()) {
            return Map.of();
        }

        return issueTypeService.getVisibleIssueTypesByIds(issueTypeIds, tenantId).stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, Function.identity()));
    }

    private Map<Long, List<WorkflowSettingsOverviewView.SchemeRefView>> buildSchemeRefsByWorkflowId(
            List<WorkflowSchemeEntity> schemes) {
        Map<Long, List<WorkflowSettingsOverviewView.SchemeRefView>> refsByWorkflowId = new LinkedHashMap<>();
        for (WorkflowSchemeEntity scheme : schemes) {
            WorkflowSettingsOverviewView.SchemeRefView schemeRef =
                    new WorkflowSettingsOverviewView.SchemeRefView(
                            scheme.getId(),
                            scheme.getName(),
                            scheme.isSystem()
                    );
            for (Long workflowId : workflowIdsForScheme(scheme)) {
                refsByWorkflowId.computeIfAbsent(workflowId, ignored -> new ArrayList<>())
                        .add(schemeRef);
            }
        }
        return refsByWorkflowId;
    }

    private Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> buildProjectRefsBySchemeId(
            Long tenantId,
            List<WorkflowSchemeEntity> schemes) {
        List<Long> schemeIds = schemes.stream()
                .map(WorkflowSchemeEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (schemeIds.isEmpty()) {
            return Map.of();
        }

        return projectReadPort.getActiveProjectsByWorkflowSchemeIds(schemeIds, tenantId).stream()
                .filter(project -> project.getWorkflowSchemeId() != null)
                .collect(Collectors.groupingBy(
                        ProjectEntity::getWorkflowSchemeId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                project -> new WorkflowSettingsOverviewView.ProjectRefView(
                                        project.getId(),
                                        project.getKey(),
                                        project.getName()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> buildProjectRefsByWorkflowId(
            List<WorkflowSchemeEntity> schemes,
            Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> projectRefsBySchemeId) {
        Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> refsByWorkflowId = new LinkedHashMap<>();
        for (WorkflowSchemeEntity scheme : schemes) {
            List<WorkflowSettingsOverviewView.ProjectRefView> projects =
                    projectRefsBySchemeId.getOrDefault(scheme.getId(), List.of());
            if (projects.isEmpty()) {
                continue;
            }
            for (Long workflowId : workflowIdsForScheme(scheme)) {
                refsByWorkflowId.computeIfAbsent(workflowId, ignored -> new ArrayList<>())
                        .addAll(projects);
            }
        }
        return refsByWorkflowId;
    }

    private WorkflowSettingsOverviewView.WorkflowView toWorkflowView(
            WorkflowEntity workflow,
            Map<Long, List<WorkflowSettingsOverviewView.SchemeRefView>> schemeRefsByWorkflowId,
            Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> projectRefsByWorkflowId) {
        return new WorkflowSettingsOverviewView.WorkflowView(
                workflow.getId(),
                workflow.getTenantId(),
                workflow.getWorkflowKey(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getCurrentPublishedVersionId(),
                workflow.getDraftVersionId(),
                workflow.getLifecycleState(),
                Boolean.TRUE.equals(workflow.getIsSystem()),
                Boolean.TRUE.equals(workflow.getIsSystem()),
                schemeRefsByWorkflowId.getOrDefault(workflow.getId(), List.of()),
                projectRefsByWorkflowId.getOrDefault(workflow.getId(), List.of()),
                workflow.getCreatedAt(),
                workflow.getCreatedBy(),
                workflow.getUpdatedAt(),
                workflow.getUpdatedBy()
        );
    }

    private WorkflowSettingsOverviewView.WorkflowSchemeView toWorkflowSchemeView(
            WorkflowSchemeEntity scheme,
            Map<Long, IssueTypeEntity> issueTypesById,
            Map<Long, WorkflowEntity> workflowsById,
            Map<Long, List<WorkflowSettingsOverviewView.ProjectRefView>> projectRefsBySchemeId) {
        List<WorkflowSettingsOverviewView.WorkflowSchemeItemView> items = safeItems(scheme).stream()
                .sorted(Comparator
                        .comparing((WorkflowSchemeItemEntity item) ->
                                item.getIssueTypeId() == null ? Long.MAX_VALUE : item.getIssueTypeId())
                        .thenComparing(item ->
                                item.getWorkflowId() == null ? Long.MAX_VALUE : item.getWorkflowId()))
                .map(item -> toWorkflowSchemeItemView(
                        item,
                        issueTypesById.get(item.getIssueTypeId()),
                        workflowsById.get(item.getWorkflowId())
                ))
                .toList();

        return new WorkflowSettingsOverviewView.WorkflowSchemeView(
                scheme.getId(),
                scheme.getTenantId(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getDefaultWorkflowId(),
                scheme.isSystem(),
                scheme.isSystem(),
                items,
                projectRefsBySchemeId.getOrDefault(scheme.getId(), List.of()),
                scheme.getCreatedAt(),
                scheme.getCreatedBy(),
                scheme.getUpdatedAt(),
                scheme.getUpdatedBy()
        );
    }

    private WorkflowSettingsOverviewView.WorkflowSchemeItemView toWorkflowSchemeItemView(
            WorkflowSchemeItemEntity item,
            IssueTypeEntity issueType,
            WorkflowEntity workflow) {
        return new WorkflowSettingsOverviewView.WorkflowSchemeItemView(
                item.getId(),
                item.getIssueTypeId(),
                item.getWorkflowId(),
                toWorkTypeOptionView(issueType),
                toWorkflowOptionView(workflow)
        );
    }

    private WorkflowSettingsOverviewView.WorkTypeOptionView toWorkTypeOptionView(IssueTypeEntity issueType) {
        if (issueType == null) {
            return null;
        }
        return new WorkflowSettingsOverviewView.WorkTypeOptionView(
                issueType.getId(),
                issueType.getTypeKey(),
                issueType.getName(),
                issueType.getHierarchyLevel(),
                issueType.isSystem(),
                issueType.isSystem()
        );
    }

    private WorkflowSettingsOverviewView.WorkflowOptionView toWorkflowOptionView(WorkflowEntity workflow) {
        if (workflow == null) {
            return null;
        }
        return new WorkflowSettingsOverviewView.WorkflowOptionView(
                workflow.getId(),
                workflow.getWorkflowKey(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getCurrentPublishedVersionId(),
                workflow.getDraftVersionId(),
                workflow.getLifecycleState(),
                Boolean.TRUE.equals(workflow.getIsSystem()),
                Boolean.TRUE.equals(workflow.getIsSystem())
        );
    }

    private Set<Long> workflowIdsForScheme(WorkflowSchemeEntity scheme) {
        Set<Long> workflowIds = new LinkedHashSet<>();
        if (scheme.getDefaultWorkflowId() != null) {
            workflowIds.add(scheme.getDefaultWorkflowId());
        }
        safeItems(scheme).stream()
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .filter(Objects::nonNull)
                .forEach(workflowIds::add);
        return workflowIds;
    }

    private List<WorkflowSchemeItemEntity> safeItems(WorkflowSchemeEntity scheme) {
        return scheme.getItems() == null ? List.of() : scheme.getItems();
    }
}
