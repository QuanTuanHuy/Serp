/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.createmeta;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workitem.command.create.support.CreateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldOptionPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWorkItemCreateMetaQueryHandler
        implements IQueryHandler<GetWorkItemCreateMetaQuery, WorkItemCreateMetaView> {

    private static final int COMPONENT_PAGE_SIZE = 200;

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueTypeSchemeService issueTypeSchemeService;
    private final IIssueTypePort issueTypePort;
    private final WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver;
    private final IStatusService statusService;
    private final IPrioritySchemeService prioritySchemeService;
    private final IPriorityPort priorityPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final IProjectComponentService projectComponentService;
    private final CreateWorkItemFieldRulesResolver createWorkItemFieldRulesResolver;
    private final ICustomFieldPort customFieldPort;
    private final ICustomFieldContextPort customFieldContextPort;
    private final ICustomFieldOptionPort customFieldOptionPort;
    private final ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;

    @Override
    @Transactional(readOnly = true)
    public WorkItemCreateMetaView handle(GetWorkItemCreateMetaQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                query.userId(),
                safeGroupKeys(query.groupKeys())
        );

        projectPermissionEvaluationService.checkPermission(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        IssueTypeSchemeEntity issueTypeScheme = issueTypeSchemeService.getVisibleIssueTypeSchemeDetailById(
                requireSchemeId(project.getIssueTypeSchemeId(), DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND, "Project has no issue type scheme binding"),
                query.tenantId()
        );
        List<IssueTypeEntity> issueTypes = loadIssueTypes(issueTypeScheme, query.tenantId());
        Long selectedIssueTypeId = resolveSelectedIssueTypeId(query.issueTypeId(), issueTypeScheme, issueTypes);
        IssueTypeEntity selectedIssueType = requireSelectedIssueType(selectedIssueTypeId, issueTypes);

        WorkflowStepEntity initialStep = workItemCreateConfigurationResolver.resolveInitialWorkflowStep(
                project,
                selectedIssueTypeId,
                query.tenantId()
        );
        StatusEntity initialStatus = statusService.getVisibleStatusById(initialStep.getStatusId(), query.tenantId());

        PrioritySchemeEntity priorityScheme = loadPriorityScheme(project, query.tenantId());
        List<PriorityEntity> priorities = loadPriorities(priorityScheme, query.tenantId());
        Long defaultPriorityId = priorityScheme == null ? null : priorityScheme.getDefaultPriorityId();

        IssueSecuritySchemeEntity issueSecurityScheme = loadIssueSecurityScheme(project, query.tenantId());
        List<IssueSecurityLevelEntity> securityLevels = loadSecurityLevels(project, query.tenantId());
        Long defaultSecurityLevelId = issueSecurityScheme == null ? null : issueSecurityScheme.getDefaultLevelId();

        List<ProjectComponentEntity> components = loadProjectComponents(project, query.tenantId());
        WorkItemFieldRules fieldRules = createWorkItemFieldRulesResolver.resolveCreateFieldRules(
                project,
                selectedIssueTypeId,
                query.tenantId()
        );

        boolean canCreate = projectPermissionEvaluationService.hasPermission(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.CREATE_ISSUES
        );
        boolean projectArchived = Boolean.TRUE.equals(project.getIsArchived());
        String blockedReason = null;
        if (projectArchived) {
            blockedReason = "Project is archived.";
        } else if (!canCreate) {
            blockedReason = "Current user does not have Create Issues permission in this project.";
        }

        return new WorkItemCreateMetaView(
                WorkItemCreateMetaView.ProjectSummaryView.from(project),
                canCreate && !projectArchived,
                blockedReason,
                issueTypes.stream()
                        .map(WorkItemCreateMetaView.IssueTypeOptionView::from)
                        .toList(),
                selectedIssueTypeId,
                WorkItemCreateMetaView.StatusOptionView.from(initialStatus),
                defaultPriorityId,
                priorities.stream()
                        .map(WorkItemCreateMetaView.PriorityOptionView::from)
                        .toList(),
                defaultSecurityLevelId,
                securityLevels.stream()
                        .map(WorkItemCreateMetaView.SecurityLevelOptionView::from)
                        .toList(),
                components.stream()
                        .map(WorkItemCreateMetaView.ComponentOptionView::from)
                        .toList(),
                toSystemFieldViews(fieldRules),
                toCustomFieldViews(selectedIssueType, fieldRules)
        );
    }

    private List<IssueTypeEntity> loadIssueTypes(IssueTypeSchemeEntity issueTypeScheme,
                                                 Long tenantId) {
        List<Long> issueTypeIds = issueTypeScheme.getItems() == null
                ? List.of()
                : issueTypeScheme.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getSequence() == null ? Integer.MAX_VALUE : item.getSequence()))
                .map(serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity::getIssueTypeId)
                .distinct()
                .toList();

        if (issueTypeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, IssueTypeEntity> issueTypesById = issueTypePort
                .getIssueTypesByIdsIncludingSystem(issueTypeIds, tenantId)
                .stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, Function.identity()));

        List<IssueTypeEntity> orderedIssueTypes = new ArrayList<>();
        for (Long issueTypeId : issueTypeIds) {
            IssueTypeEntity entity = issueTypesById.get(issueTypeId);
            if (entity != null) {
                orderedIssueTypes.add(entity);
            }
        }
        return orderedIssueTypes;
    }

    private Long resolveSelectedIssueTypeId(Long requestedIssueTypeId,
                                            IssueTypeSchemeEntity issueTypeScheme,
                                            List<IssueTypeEntity> issueTypes) {
        if (requestedIssueTypeId != null) {
            return requestedIssueTypeId;
        }
        if (issueTypeScheme.getDefaultIssueTypeId() != null) {
            return issueTypeScheme.getDefaultIssueTypeId();
        }
        if (!issueTypes.isEmpty()) {
            return issueTypes.getFirst().getId();
        }
        throw new DomainValidationException(
                DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME,
                "Issue type scheme does not contain any issue types."
        );
    }

    private IssueTypeEntity requireSelectedIssueType(Long selectedIssueTypeId,
                                                     List<IssueTypeEntity> issueTypes) {
        return issueTypes.stream()
                .filter(issueType -> selectedIssueTypeId.equals(issueType.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME,
                        "Selected issue type is not allowed in the project scheme: issueTypeId=" + selectedIssueTypeId
                ));
    }

    private PrioritySchemeEntity loadPriorityScheme(ProjectEntity project,
                                                    Long tenantId) {
        if (project.getPrioritySchemeId() == null) {
            return null;
        }
        return prioritySchemeService.getVisiblePrioritySchemeDetailById(project.getPrioritySchemeId(), tenantId);
    }

    private List<PriorityEntity> loadPriorities(PrioritySchemeEntity priorityScheme,
                                                Long tenantId) {
        if (priorityScheme == null || priorityScheme.getItems() == null || priorityScheme.getItems().isEmpty()) {
            return List.of();
        }

        List<Long> priorityIds = priorityScheme.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getSequence() == null ? Integer.MAX_VALUE : item.getSequence()))
                .map(PrioritySchemeItemEntity::getPriorityId)
                .distinct()
                .toList();

        Map<Long, PriorityEntity> prioritiesById = priorityPort
                .getPrioritiesByIdsIncludingSystem(priorityIds, tenantId)
                .stream()
                .collect(Collectors.toMap(PriorityEntity::getId, Function.identity()));

        List<PriorityEntity> orderedPriorities = new ArrayList<>();
        for (Long priorityId : priorityIds) {
            PriorityEntity entity = prioritiesById.get(priorityId);
            if (entity != null) {
                orderedPriorities.add(entity);
            }
        }
        return orderedPriorities;
    }

    private IssueSecuritySchemeEntity loadIssueSecurityScheme(ProjectEntity project,
                                                              Long tenantId) {
        if (project.getIssueSecuritySchemeId() == null) {
            return null;
        }
        return issueSecuritySchemePort
                .getIssueSecuritySchemeByIdIncludingSystem(project.getIssueSecuritySchemeId(), tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + project.getIssueSecuritySchemeId()
                ));
    }

    private List<IssueSecurityLevelEntity> loadSecurityLevels(ProjectEntity project,
                                                              Long tenantId) {
        if (project.getIssueSecuritySchemeId() == null) {
            return List.of();
        }
        return issueSecurityLevelPort.getIssueSecurityLevelsBySchemeIdIncludingSystem(
                project.getIssueSecuritySchemeId(),
                tenantId
        );
    }

    private List<ProjectComponentEntity> loadProjectComponents(ProjectEntity project,
                                                               Long tenantId) {
        PageResult<ProjectComponentEntity> pageResult = projectComponentService.listComponents(
                project.getId(),
                tenantId,
                ProjectComponentListCriteria.builder()
                        .page(0)
                        .pageSize(COMPONENT_PAGE_SIZE)
                        .sortBy("name")
                        .sortDirection("asc")
                        .build()
        );
        return pageResult.items();
    }

    private Map<String, WorkItemCreateMetaView.FieldPolicyView> toSystemFieldViews(WorkItemFieldRules fieldRules) {
        return fieldRules.systemPolicies().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> WorkItemCreateMetaView.FieldPolicyView.from(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private List<WorkItemCreateMetaView.CustomFieldView> toCustomFieldViews(IssueTypeEntity issueType,
                                                                             WorkItemFieldRules fieldRules) {
        if (fieldRules.customPolicies().isEmpty()) {
            return List.of();
        }

        List<String> fieldKeys = fieldRules.customPolicies().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .toList();

        Map<String, CustomFieldEntity> customFieldsByKey = customFieldPort
                .getCustomFieldsByFieldKeys(fieldKeys)
                .stream()
                .collect(Collectors.toMap(CustomFieldEntity::getFieldKey, Function.identity()));

        List<WorkItemCreateMetaView.CustomFieldView> views = new ArrayList<>();
        for (String fieldKey : fieldKeys) {
            CustomFieldEntity customField = customFieldsByKey.get(fieldKey);
            if (customField == null) {
                throw new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Custom field definition not found for field=" + fieldKey
                );
            }

            CustomFieldContextEntity context = resolveCustomFieldContext(customField, issueType.getTypeKey());
            WorkItemFieldPolicy policy = fieldRules.customPolicies().get(fieldKey);
            views.add(WorkItemCreateMetaView.CustomFieldView.from(
                    customField,
                    context,
                    policy,
                    customFieldOptionPort.getCustomFieldOptionsByContextId(context.getId()),
                    customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(context.getId())
            ));
        }
        return views;
    }

    private CustomFieldContextEntity resolveCustomFieldContext(CustomFieldEntity customField,
                                                               String issueTypeKey) {
        List<CustomFieldContextEntity> applicableContexts = customFieldContextPort
                .getApplicableCustomFieldContexts(customField.getId(), issueTypeKey);

        List<CustomFieldContextEntity> exactContexts = applicableContexts.stream()
                .filter(context -> issueTypeKey.equals(normalizeNullable(context.getIssueTypeKey())))
                .toList();
        if (exactContexts.size() == 1) {
            return exactContexts.getFirst();
        }
        if (exactContexts.size() > 1) {
            throw unresolvedCustomFieldContext(customField.getFieldKey(), issueTypeKey);
        }

        List<CustomFieldContextEntity> globalContexts = applicableContexts.stream()
                .filter(context -> context.getIssueTypeKey() == null)
                .toList();
        if (globalContexts.size() == 1) {
            return globalContexts.getFirst();
        }
        if (globalContexts.isEmpty()) {
            throw unresolvedCustomFieldContext(customField.getFieldKey(), issueTypeKey);
        }
        throw unresolvedCustomFieldContext(customField.getFieldKey(), issueTypeKey);
    }

    private DomainValidationException unresolvedCustomFieldContext(String fieldKey,
                                                                   String issueTypeKey) {
        return new DomainValidationException(
                DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                "Custom field context is unresolvable for field=" + fieldKey + ", issueTypeKey=" + issueTypeKey
        );
    }

    private Long requireSchemeId(Long schemeId,
                                 DomainErrorCode errorCode,
                                 String message) {
        if (schemeId == null) {
            throw new DomainValidationException(errorCode, message);
        }
        return schemeId;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private Set<String> safeGroupKeys(Set<String> groupKeys) {
        return groupKeys == null ? Set.of() : groupKeys;
    }
}
