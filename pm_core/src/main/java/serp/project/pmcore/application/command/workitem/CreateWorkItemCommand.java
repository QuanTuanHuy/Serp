/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.workitem.validator.CreateWorkItemValidator;
import serp.project.pmcore.domain.constant.EventConstants;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.domain.dto.response.workitem.WorkItemResponse;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.entity.FieldConfigEntity;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.entity.OutboxEventEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;
import serp.project.pmcore.domain.enums.OutboxEventStatus;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.domain.port.store.IFieldConfigItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.port.store.IFieldConfigSchemePort;
import serp.project.pmcore.domain.port.store.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IScreenPort;
import serp.project.pmcore.domain.port.store.IScreenSchemeItemPort;
import serp.project.pmcore.domain.port.store.IScreenSchemePort;
import serp.project.pmcore.domain.port.store.IScreenTabFieldPort;
import serp.project.pmcore.domain.port.store.IScreenTabPort;
import serp.project.pmcore.domain.port.store.IWorkflowPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
import serp.project.pmcore.domain.port.store.IWorkflowVersionPort;
import serp.project.pmcore.domain.port.store.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.service.IOutboxEventService;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CreateWorkItemCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateWorkItemCommand.class);

    private static final String CREATE_OPERATION_KEY = "CREATE";
    private static final String FIELD_REF_TYPE_SYSTEM = "SYSTEM";
    private static final String FIELD_REF_TYPE_CUSTOM = "CUSTOM";

    private static final String VALUE_TYPE_TEXT = "TEXT";
    private static final String VALUE_TYPE_NUMBER = "NUMBER";
    private static final String VALUE_TYPE_DATE = "DATE";
    private static final String VALUE_TYPE_DATETIME = "DATETIME";
    private static final String VALUE_TYPE_USER = "USER";
    private static final String VALUE_TYPE_GROUP = "GROUP";
    private static final String VALUE_TYPE_OPTION = "OPTION";
    private static final String VALUE_TYPE_JSON = "JSON";

    private static final String SYSTEM_FIELD_ISSUE_TYPE_ID = "issue_type_id";
    private static final String SYSTEM_FIELD_SUMMARY = "summary";
    private static final String SYSTEM_FIELD_DESCRIPTION = "description";
    private static final String SYSTEM_FIELD_PRIORITY_ID = "priority_id";
    private static final String SYSTEM_FIELD_ASSIGNEE_ID = "assignee_id";
    private static final String SYSTEM_FIELD_PARENT_ID = "parent_id";
    private static final String SYSTEM_FIELD_DUE_DATE = "due_date";
    private static final String SYSTEM_FIELD_TIME_ORIGINAL_ESTIMATE = "time_original_estimate";
    private static final String SYSTEM_FIELD_SECURITY_LEVEL_ID = "security_level_id";

    private static final Set<String> ALWAYS_WRITABLE_SYSTEM_FIELDS = Set.of(
            SYSTEM_FIELD_ISSUE_TYPE_ID,
            SYSTEM_FIELD_SUMMARY
    );

    private static final Set<String> SUPPORTED_SYSTEM_FIELDS = Set.of(
            SYSTEM_FIELD_ISSUE_TYPE_ID,
            SYSTEM_FIELD_SUMMARY,
            SYSTEM_FIELD_DESCRIPTION,
            SYSTEM_FIELD_PRIORITY_ID,
            SYSTEM_FIELD_ASSIGNEE_ID,
            SYSTEM_FIELD_PARENT_ID,
            SYSTEM_FIELD_DUE_DATE,
            SYSTEM_FIELD_TIME_ORIGINAL_ESTIMATE,
            SYSTEM_FIELD_SECURITY_LEVEL_ID
    );

    private final CreateWorkItemValidator createWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IFieldConfigPort fieldConfigPort;
    private final IFieldConfigItemPort fieldConfigItemPort;
    private final IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IScreenSchemePort screenSchemePort;
    private final IScreenSchemeItemPort screenSchemeItemPort;
    private final IScreenPort screenPort;
    private final IScreenTabPort screenTabPort;
    private final IScreenTabFieldPort screenTabFieldPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final ICustomFieldPort customFieldPort;
    private final ICustomFieldContextPort customFieldContextPort;
    private final ICustomFieldOptionPort customFieldOptionPort;
    private final ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    private final IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public CreateWorkItemCommand(CreateWorkItemValidator createWorkItemValidator,
                                 IProjectService projectService,
                                 IWorkItemService workItemService,
                                 IProjectPermissionEvaluationService projectPermissionEvaluationService,
                                 IIssueTypePort issueTypePort,
                                 IIssueTypeSchemeItemPort issueTypeSchemeItemPort,
                                 IWorkflowSchemePort workflowSchemePort,
                                 IWorkflowSchemeItemPort workflowSchemeItemPort,
                                 IWorkflowPort workflowPort,
                                 IWorkflowVersionPort workflowVersionPort,
                                 IWorkflowStepPort workflowStepPort,
                                 IFieldConfigSchemePort fieldConfigSchemePort,
                                 IFieldConfigSchemeItemPort fieldConfigSchemeItemPort,
                                 IFieldConfigPort fieldConfigPort,
                                 IFieldConfigItemPort fieldConfigItemPort,
                                 IIssueTypeScreenSchemePort issueTypeScreenSchemePort,
                                 IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort,
                                 IScreenSchemePort screenSchemePort,
                                 IScreenSchemeItemPort screenSchemeItemPort,
                                 IScreenPort screenPort,
                                 IScreenTabPort screenTabPort,
                                 IScreenTabFieldPort screenTabFieldPort,
                                 IPrioritySchemePort prioritySchemePort,
                                 IPrioritySchemeItemPort prioritySchemeItemPort,
                                 IIssueSecuritySchemePort issueSecuritySchemePort,
                                 IIssueSecurityLevelPort issueSecurityLevelPort,
                                 ICustomFieldPort customFieldPort,
                                 ICustomFieldContextPort customFieldContextPort,
                                 ICustomFieldOptionPort customFieldOptionPort,
                                 ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort,
                                 IWorkItemCustomFieldValuePort workItemCustomFieldValuePort,
                                 IOutboxEventService outboxEventService,
                                 JsonUtils jsonUtils) {
        this.createWorkItemValidator = createWorkItemValidator;
        this.projectService = projectService;
        this.workItemService = workItemService;
        this.projectPermissionEvaluationService = projectPermissionEvaluationService;
        this.issueTypePort = issueTypePort;
        this.issueTypeSchemeItemPort = issueTypeSchemeItemPort;
        this.workflowSchemePort = workflowSchemePort;
        this.workflowSchemeItemPort = workflowSchemeItemPort;
        this.workflowPort = workflowPort;
        this.workflowVersionPort = workflowVersionPort;
        this.workflowStepPort = workflowStepPort;
        this.fieldConfigSchemePort = fieldConfigSchemePort;
        this.fieldConfigSchemeItemPort = fieldConfigSchemeItemPort;
        this.fieldConfigPort = fieldConfigPort;
        this.fieldConfigItemPort = fieldConfigItemPort;
        this.issueTypeScreenSchemePort = issueTypeScreenSchemePort;
        this.issueTypeScreenSchemeItemPort = issueTypeScreenSchemeItemPort;
        this.screenSchemePort = screenSchemePort;
        this.screenSchemeItemPort = screenSchemeItemPort;
        this.screenPort = screenPort;
        this.screenTabPort = screenTabPort;
        this.screenTabFieldPort = screenTabFieldPort;
        this.prioritySchemePort = prioritySchemePort;
        this.prioritySchemeItemPort = prioritySchemeItemPort;
        this.issueSecuritySchemePort = issueSecuritySchemePort;
        this.issueSecurityLevelPort = issueSecurityLevelPort;
        this.customFieldPort = customFieldPort;
        this.customFieldContextPort = customFieldContextPort;
        this.customFieldOptionPort = customFieldOptionPort;
        this.customFieldContextDefaultValuePort = customFieldContextDefaultValuePort;
        this.workItemCustomFieldValuePort = workItemCustomFieldValuePort;
        this.outboxEventService = outboxEventService;
        this.jsonUtils = jsonUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkItemResponse execute(Long projectId,
                                    CreateWorkItemRequest request,
                                    Long tenantId,
                                    Long userId,
                                    Set<String> groupKeys) {
        createWorkItemValidator.validate(request);

        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        ensureProjectWritable(project);

        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.CREATE_ISSUES);

        IssueTypeEntity issueType = issueTypePort.getIssueTypeById(request.getIssueTypeId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(request.getIssueTypeId()));
        validateIssueTypeInProjectScheme(project, issueType.getId(), tenantId);
        validateParentRequirement(issueType, request.getParentId());

        WorkflowStepEntity initialStep = resolveInitialWorkflowStep(project, issueType.getId(), tenantId);
        CreateFieldRules createFieldRules = resolveCreateFieldRules(project, issueType.getId(), tenantId);
        validateClientSuppliedWritableFields(request, createFieldRules);

        Long priorityId = resolvePriorityId(project, request.getPriorityId(), tenantId);

        if (request.getDueDate() != null) {
            projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
        }

        Long assigneeId = resolveAssigneeId(project, request.getAssigneeId(), actorContext, tenantId);
        Long securityLevelId = resolveSecurityLevelId(project, request.getSecurityLevelId(), actorContext, tenantId);

        if (request.getParentId() != null) {
            workItemService.validateParentHierarchy(request.getParentId(), issueType.getId(), projectId, tenantId);
        }

        ResolvedCustomFields resolvedCustomFields = resolveCustomFields(
                projectId,
                issueType.getId(),
                request.getCustomFields(),
                createFieldRules,
                tenantId
        );
        validateRequiredFields(request, priorityId, assigneeId, securityLevelId, createFieldRules, resolvedCustomFields);

        long issueNo = workItemService.getNextIssueNumber(projectId, tenantId);
        String key = project.getKey() + "-" + issueNo;
        String rank = workItemService.getNextRank(projectId, tenantId);

        WorkItemEntity workItem = WorkItemEntity.builder()
                .projectId(projectId)
                .issueTypeId(request.getIssueTypeId())
                .issueNo(issueNo)
                .key(key)
                .summary(request.getSummary())
                .description(request.getDescription())
                .workflowStepId(initialStep.getId())
                .statusId(initialStep.getStatusId())
                .priorityId(priorityId)
                .resolutionId(null)
                .assigneeId(assigneeId)
                .reporterId(userId)
                .parentId(request.getParentId())
                .securityLevelId(securityLevelId)
                .dueDate(request.getDueDate())
                .rank(rank)
                .timeOriginalEstimate(request.getTimeOriginalEstimate())
                .timeRemainingEstimate(request.getTimeOriginalEstimate())
                .timeSpent(0L)
                .build();

        WorkItemEntity savedWorkItem = workItemService.createWorkItem(workItem, tenantId, userId);
        persistCustomFieldValues(savedWorkItem.getId(), resolvedCustomFields.values(), tenantId, userId);
        persistCreatedOutboxEvent(savedWorkItem, tenantId, userId, projectId);

        log.info("Created work item id={} key={} projectId={} tenantId={}",
                savedWorkItem.getId(), savedWorkItem.getKey(), projectId, tenantId);

        return WorkItemResponse.from(savedWorkItem);
    }

    private CreateFieldRules resolveCreateFieldRules(ProjectEntity project, Long issueTypeId, Long tenantId) {
        Long fieldConfigId = resolveFieldConfigId(project, issueTypeId, tenantId);
        Long createScreenId = resolveCreateScreenId(project, issueTypeId, tenantId);

        List<FieldConfigItemEntity> fieldConfigItems = fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(fieldConfigId, tenantId);
        List<FieldRef> createScreenFields = loadCreateScreenFields(createScreenId, tenantId);

        Map<String, FieldPolicy> systemPolicies = new LinkedHashMap<>();
        Map<String, FieldPolicy> customPolicies = new LinkedHashMap<>();

        for (FieldConfigItemEntity fieldConfigItem : fieldConfigItems) {
            FieldRef fieldRef = normalizeFieldRef(fieldConfigItem.getFieldRefType(), fieldConfigItem.getFieldRef());
            if (fieldRef == null) {
                continue;
            }
            mergeFieldPolicy(
                    fieldRef,
                    Boolean.TRUE.equals(fieldConfigItem.getIsRequired()),
                    Boolean.TRUE.equals(fieldConfigItem.getIsHidden()),
                    false,
                    systemPolicies,
                    customPolicies
            );
        }

        for (FieldRef createScreenField : createScreenFields) {
            mergeFieldPolicy(createScreenField, false, false, true, systemPolicies, customPolicies);
        }

        mergeFieldPolicy(
                new FieldRef(FIELD_REF_TYPE_SYSTEM, SYSTEM_FIELD_SUMMARY),
                true,
                false,
                true,
                systemPolicies,
                customPolicies
        );
        mergeFieldPolicy(
                new FieldRef(FIELD_REF_TYPE_SYSTEM, SYSTEM_FIELD_ISSUE_TYPE_ID),
                false,
                false,
                true,
                systemPolicies,
                customPolicies
        );

        return new CreateFieldRules(systemPolicies, customPolicies);
    }

    private Long resolveFieldConfigId(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getFieldConfigSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                    "Project has no field configuration scheme binding: projectId=" + project.getId()
            );
        }

        FieldConfigSchemeEntity fieldConfigScheme = fieldConfigSchemePort
                .getFieldConfigSchemeById(project.getFieldConfigSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                        "Field configuration scheme not found: id=" + project.getFieldConfigSchemeId()
                ));

        Long fieldConfigId = fieldConfigSchemeItemPort.getFieldConfigSchemeItemsBySchemeId(project.getFieldConfigSchemeId(), tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(FieldConfigSchemeItemEntity::getFieldConfigId)
                .findFirst()
                .orElse(fieldConfigScheme.getDefaultFieldConfigId());

        if (fieldConfigId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_COVERAGE_MISSING,
                    "Field configuration scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        fieldConfigPort.getFieldConfigById(fieldConfigId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.FIELD_CONFIG_NOT_FOUND,
                        "Field configuration not found: id=" + fieldConfigId
                ));

        return fieldConfigId;
    }

    private Long resolveCreateScreenId(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getIssueTypeScreenSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                    "Project has no issue type screen scheme binding: projectId=" + project.getId()
            );
        }

        IssueTypeScreenSchemeEntity issueTypeScreenScheme = issueTypeScreenSchemePort
                .getIssueTypeScreenSchemeById(project.getIssueTypeScreenSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND,
                        "Issue type screen scheme not found: id=" + project.getIssueTypeScreenSchemeId()
                ));

        Long screenSchemeId = issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(project.getIssueTypeScreenSchemeId(), tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(IssueTypeScreenSchemeItemEntity::getScreenSchemeId)
                .findFirst()
                .orElse(issueTypeScreenScheme.getDefaultScreenSchemeId());

        if (screenSchemeId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.ISSUE_TYPE_SCREEN_SCHEME_COVERAGE_MISSING,
                    "Issue type screen scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        ScreenSchemeEntity screenScheme = screenSchemePort.getScreenSchemeById(screenSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_SCHEME_NOT_FOUND,
                        "Screen scheme not found: id=" + screenSchemeId
                ));

        Long createScreenId = screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(screenSchemeId, tenantId)
                .stream()
                .filter(item -> CREATE_OPERATION_KEY.equalsIgnoreCase(item.getOperationKey()))
                .map(ScreenSchemeItemEntity::getScreenId)
                .findFirst()
                .orElse(screenScheme.getDefaultScreenId());

        if (createScreenId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCREEN_SCHEME_OPERATION_COVERAGE_MISSING,
                    "CREATE screen is not resolvable for screenSchemeId=" + screenSchemeId
            );
        }

        screenPort.getScreenById(createScreenId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCREEN_NOT_FOUND,
                        "CREATE screen not found: id=" + createScreenId
                ));

        return createScreenId;
    }

    private List<FieldRef> loadCreateScreenFields(Long createScreenId, Long tenantId) {
        List<ScreenTabEntity> screenTabs = screenTabPort.getScreenTabsByScreenId(createScreenId, tenantId);
        List<FieldRef> fieldRefs = new ArrayList<>();
        for (ScreenTabEntity screenTab : screenTabs) {
            List<ScreenTabFieldEntity> screenTabFields = screenTabFieldPort
                    .getScreenTabFieldsByScreenTabId(screenTab.getId(), tenantId);
            for (ScreenTabFieldEntity screenTabField : screenTabFields) {
                FieldRef fieldRef = normalizeFieldRef(screenTabField.getFieldRefType(), screenTabField.getFieldRef());
                if (fieldRef != null) {
                    fieldRefs.add(fieldRef);
                }
            }
        }
        return fieldRefs;
    }

    private FieldRef normalizeFieldRef(String fieldRefType, String fieldRef) {
        if (fieldRefType == null || fieldRef == null || fieldRef.isBlank()) {
            return null;
        }

        String normalizedType = normalizeToken(fieldRefType);
        if (FIELD_REF_TYPE_SYSTEM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new FieldRef(FIELD_REF_TYPE_SYSTEM, canonicalizeSystemFieldRef(fieldRef));
        }

        if (FIELD_REF_TYPE_CUSTOM.toLowerCase(Locale.ROOT).equals(normalizedType)) {
            return new FieldRef(FIELD_REF_TYPE_CUSTOM, fieldRef.trim());
        }

        return null;
    }

    private String canonicalizeSystemFieldRef(String fieldRef) {
        String normalized = normalizeToken(fieldRef);
        return switch (normalized) {
            case "issue_type_id", "issue_type", "issuetype" -> SYSTEM_FIELD_ISSUE_TYPE_ID;
            case "summary" -> SYSTEM_FIELD_SUMMARY;
            case "description" -> SYSTEM_FIELD_DESCRIPTION;
            case "priority_id", "priority" -> SYSTEM_FIELD_PRIORITY_ID;
            case "assignee_id", "assignee" -> SYSTEM_FIELD_ASSIGNEE_ID;
            case "parent_id", "parent" -> SYSTEM_FIELD_PARENT_ID;
            case "due_date", "due" -> SYSTEM_FIELD_DUE_DATE;
            case "time_original_estimate", "original_estimate" -> SYSTEM_FIELD_TIME_ORIGINAL_ESTIMATE;
            case "security_level_id", "security_level", "security" -> SYSTEM_FIELD_SECURITY_LEVEL_ID;
            default -> normalized;
        };
    }

    private String normalizeToken(String value) {
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void mergeFieldPolicy(FieldRef fieldRef,
                                  boolean required,
                                  boolean hidden,
                                  boolean onCreateScreen,
                                  Map<String, FieldPolicy> systemPolicies,
                                  Map<String, FieldPolicy> customPolicies) {
        Map<String, FieldPolicy> targetPolicies = FIELD_REF_TYPE_SYSTEM.equals(fieldRef.fieldRefType())
                ? systemPolicies
                : customPolicies;

        FieldPolicy existingPolicy = targetPolicies.get(fieldRef.fieldRef());
        boolean mergedRequired = required;
        boolean mergedHidden = hidden;
        boolean mergedOnCreateScreen = onCreateScreen;

        if (existingPolicy != null) {
            mergedRequired = existingPolicy.required() || mergedRequired;
            mergedHidden = existingPolicy.hidden() || mergedHidden;
            mergedOnCreateScreen = existingPolicy.onCreateScreen() || mergedOnCreateScreen;
        }

        if (FIELD_REF_TYPE_SYSTEM.equals(fieldRef.fieldRefType())
                && ALWAYS_WRITABLE_SYSTEM_FIELDS.contains(fieldRef.fieldRef())) {
            mergedHidden = false;
            mergedOnCreateScreen = true;
        }

        if (SYSTEM_FIELD_SUMMARY.equals(fieldRef.fieldRef())) {
            mergedRequired = true;
        }

        targetPolicies.put(fieldRef.fieldRef(), new FieldPolicy(
                fieldRef.fieldRefType(),
                fieldRef.fieldRef(),
                mergedRequired,
                mergedHidden,
                mergedOnCreateScreen
        ));
    }

    private void validateClientSuppliedWritableFields(CreateWorkItemRequest request, CreateFieldRules createFieldRules) {
        validateSystemFieldWritable(SYSTEM_FIELD_DESCRIPTION, request.getDescription(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_PRIORITY_ID, request.getPriorityId(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_ASSIGNEE_ID, request.getAssigneeId(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_PARENT_ID, request.getParentId(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_DUE_DATE, request.getDueDate(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate(), createFieldRules);
        validateSystemFieldWritable(SYSTEM_FIELD_SECURITY_LEVEL_ID, request.getSecurityLevelId(), createFieldRules);

        if (request.getCustomFields() == null || request.getCustomFields().isEmpty()) {
            return;
        }

        for (String customFieldKey : request.getCustomFields().keySet()) {
            FieldPolicy fieldPolicy = createFieldRules.customPolicies().get(customFieldKey);
            if (!isCustomFieldClientWritable(fieldPolicy)) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                        "Field is not writable on create: field=" + customFieldKey
                );
            }
        }
    }

    private void validateSystemFieldWritable(String fieldRef, Object value, CreateFieldRules createFieldRules) {
        if (value == null) {
            return;
        }

        FieldPolicy fieldPolicy = createFieldRules.systemPolicies().get(fieldRef);
        if (!isSystemFieldClientWritable(fieldRef, fieldPolicy)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                    "Field is not writable on create: field=" + fieldRef
            );
        }
    }

    private boolean isSystemFieldClientWritable(String fieldRef, FieldPolicy fieldPolicy) {
        if (ALWAYS_WRITABLE_SYSTEM_FIELDS.contains(fieldRef)) {
            return true;
        }
        return SUPPORTED_SYSTEM_FIELDS.contains(fieldRef)
                && fieldPolicy != null
                && !fieldPolicy.hidden()
                && fieldPolicy.onCreateScreen();
    }

    private boolean isCustomFieldClientWritable(FieldPolicy fieldPolicy) {
        return fieldPolicy != null && !fieldPolicy.hidden() && fieldPolicy.onCreateScreen();
    }

    private ResolvedCustomFields resolveCustomFields(Long projectId,
                                                     Long issueTypeId,
                                                     Map<String, Object> requestCustomFields,
                                                     CreateFieldRules createFieldRules,
                                                     Long tenantId) {
        if (createFieldRules.customPolicies().isEmpty()) {
            return new ResolvedCustomFields(List.of(), List.of());
        }

        List<String> fieldKeys = new ArrayList<>(new LinkedHashSet<>(createFieldRules.customPolicies().keySet()));
        Map<String, CustomFieldEntity> customFieldByKey = toCustomFieldMap(
                customFieldPort.getCustomFieldsByFieldKeysIncludingSystem(fieldKeys, tenantId)
        );

        List<WorkItemCustomFieldValueEntity> resolvedValues = new ArrayList<>();
        List<String> missingRequiredFields = new ArrayList<>();
        Map<String, Object> providedCustomFields = requestCustomFields == null ? Map.of() : requestCustomFields;

        for (Map.Entry<String, FieldPolicy> entry : createFieldRules.customPolicies().entrySet()) {
            String fieldKey = entry.getKey();
            FieldPolicy fieldPolicy = entry.getValue();
            CustomFieldEntity customField = customFieldByKey.get(fieldKey);

            if (customField == null) {
                throw new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Custom field definition not found for field=" + fieldKey
                );
            }

            CustomFieldContextEntity context = resolveCustomFieldContext(customField, projectId, issueTypeId, tenantId);
            List<CustomFieldContextDefaultValueEntity> defaultValues = customFieldContextDefaultValuePort
                    .getCustomFieldContextDefaultValuesByContextId(context.getId(), tenantId);
            List<CustomFieldOptionEntity> options = customFieldOptionPort.getCustomFieldOptionsByContextId(context.getId(), tenantId);

            boolean hasProvidedValue = providedCustomFields.containsKey(fieldKey) && providedCustomFields.get(fieldKey) != null;
            List<WorkItemCustomFieldValueEntity> fieldValues = hasProvidedValue
                    ? buildProvidedCustomFieldValues(customField, context.getId(), options, providedCustomFields.get(fieldKey), fieldKey)
                    : buildDefaultCustomFieldValues(customField, context.getId(), options, defaultValues, fieldKey);

            if (fieldValues.isEmpty()) {
                if (fieldPolicy.required()) {
                    missingRequiredFields.add(fieldKey);
                }
                continue;
            }

            resolvedValues.addAll(fieldValues);
        }

        return new ResolvedCustomFields(resolvedValues, missingRequiredFields);
    }

    private Map<String, CustomFieldEntity> toCustomFieldMap(List<CustomFieldEntity> customFields) {
        Map<String, CustomFieldEntity> customFieldMap = new LinkedHashMap<>();
        for (CustomFieldEntity customField : customFields) {
            CustomFieldEntity existing = customFieldMap.get(customField.getFieldKey());
            if (existing == null || (Long.valueOf(0L).equals(existing.getTenantId()) && !Long.valueOf(0L).equals(customField.getTenantId()))) {
                customFieldMap.put(customField.getFieldKey(), customField);
            }
        }
        return customFieldMap;
    }

    private CustomFieldContextEntity resolveCustomFieldContext(CustomFieldEntity customField,
                                                               Long projectId,
                                                               Long issueTypeId,
                                                               Long tenantId) {
        List<CustomFieldContextEntity> applicableContexts = customFieldContextPort
                .getApplicableCustomFieldContexts(customField.getId(), projectId, issueTypeId, tenantId);

        if (applicableContexts.isEmpty()) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "No custom field context matches field=" + customField.getFieldKey()
                            + ", projectId=" + projectId + ", issueTypeId=" + issueTypeId
            );
        }

        Map<Integer, List<CustomFieldContextEntity>> contextsBySpecificity = new LinkedHashMap<>();
        for (CustomFieldContextEntity applicableContext : applicableContexts) {
            contextsBySpecificity.computeIfAbsent(customFieldContextSpecificity(applicableContext), ignored -> new ArrayList<>())
                    .add(applicableContext);
        }

        Integer bestSpecificity = contextsBySpecificity.keySet().stream().min(Integer::compareTo)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                        "Unable to determine context specificity for field=" + customField.getFieldKey()
                ));

        List<CustomFieldContextEntity> bestContexts = contextsBySpecificity.get(bestSpecificity);
        if (bestContexts == null || bestContexts.size() != 1) {
            throw new DomainValidationException(
                    DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE,
                    "Custom field context is ambiguous for field=" + customField.getFieldKey()
                            + ", projectId=" + projectId + ", issueTypeId=" + issueTypeId
            );
        }

        return bestContexts.get(0);
    }

    private int customFieldContextSpecificity(CustomFieldContextEntity context) {
        boolean allProjects = Boolean.TRUE.equals(context.getAppliesToAllProjects());
        boolean allIssueTypes = Boolean.TRUE.equals(context.getAppliesToAllIssueTypes());

        if (!allProjects && !allIssueTypes) {
            return 1;
        }
        if (!allProjects) {
            return 2;
        }
        if (!allIssueTypes) {
            return 3;
        }
        return 4;
    }

    private List<WorkItemCustomFieldValueEntity> buildProvidedCustomFieldValues(CustomFieldEntity customField,
                                                                                Long contextId,
                                                                                List<CustomFieldOptionEntity> options,
                                                                                Object rawValue,
                                                                                String fieldKey) {
        String typeKey = normalizeToken(customField.getTypeKey());
        return switch (typeKey) {
            case "text", "url" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_TEXT,
                    requireTextValue(rawValue, fieldKey),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "number" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_NUMBER,
                    null,
                    requireNumberValue(rawValue, fieldKey),
                    null,
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "date" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_DATE,
                    null,
                    null,
                    requireDateValue(rawValue, fieldKey),
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "datetime" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_DATETIME,
                    null,
                    null,
                    null,
                    requireDateTimeValue(rawValue, fieldKey),
                    null,
                    null,
                    null,
                    0
            ));
            case "user" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_USER,
                    null,
                    null,
                    null,
                    null,
                    requireUserValue(rawValue, fieldKey),
                    null,
                    null,
                    0
            ));
            case "group" -> List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_GROUP,
                    null,
                    null,
                    null,
                    null,
                    null,
                    requireGroupValue(rawValue, fieldKey),
                    null,
                    0
            ));
            case "select" -> {
                CustomFieldOptionEntity option = resolveOption(rawValue, options, fieldKey);
                yield List.of(buildCustomFieldValue(
                        customField.getId(),
                        contextId,
                        VALUE_TYPE_OPTION,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        option.getId(),
                        0
                ));
            }
            case "multiselect" -> {
                List<?> values = asList(rawValue);
                List<WorkItemCustomFieldValueEntity> resolvedValues = new ArrayList<>();
                for (int index = 0; index < values.size(); index++) {
                    CustomFieldOptionEntity option = resolveOption(values.get(index), options, fieldKey);
                    resolvedValues.add(buildCustomFieldValue(
                            customField.getId(),
                            contextId,
                            VALUE_TYPE_OPTION,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            option.getId(),
                            index
                    ));
                }
                yield resolvedValues;
            }
            default -> List.of(buildJsonCustomFieldValue(
                    customField.getId(),
                    contextId,
                    jsonUtils.toJson(rawValue),
                    0
            ));
        };
    }

    private List<WorkItemCustomFieldValueEntity> buildDefaultCustomFieldValues(CustomFieldEntity customField,
                                                                               Long contextId,
                                                                               List<CustomFieldOptionEntity> options,
                                                                               List<CustomFieldContextDefaultValueEntity> defaultValues,
                                                                               String fieldKey) {
        if (defaultValues == null || defaultValues.isEmpty()) {
            return List.of();
        }

        String typeKey = normalizeToken(customField.getTypeKey());
        if (!"multiselect".equals(typeKey) && defaultValues.size() > 1) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Multiple default values are configured for single-value field=" + fieldKey
            );
        }

        if ("multiselect".equals(typeKey)) {
            List<WorkItemCustomFieldValueEntity> values = new ArrayList<>();
            for (int index = 0; index < defaultValues.size(); index++) {
                CustomFieldContextDefaultValueEntity defaultValue = defaultValues.get(index);
                if (defaultValue.getOptionValueId() == null) {
                    throw new BusinessRuleViolationException(
                            DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                            "Multiselect default must reference option value: field=" + fieldKey
                    );
                }

                CustomFieldOptionEntity option = resolveOption(defaultValue.getOptionValueId(), options, fieldKey);
                values.add(buildCustomFieldValue(
                        customField.getId(),
                        contextId,
                        VALUE_TYPE_OPTION,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        option.getId(),
                        defaultValue.getSortOrder() == null ? index : defaultValue.getSortOrder()
                ));
            }
            return values;
        }

        CustomFieldContextDefaultValueEntity defaultValue = defaultValues.get(0);
        return switch (typeKey) {
            case "text", "url" -> defaultValue.getTextValue() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_TEXT,
                    defaultValue.getTextValue(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "number" -> defaultValue.getNumberValue() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_NUMBER,
                    null,
                    defaultValue.getNumberValue(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "date" -> defaultValue.getDateValue() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_DATE,
                    null,
                    null,
                    defaultValue.getDateValue(),
                    null,
                    null,
                    null,
                    null,
                    0
            ));
            case "datetime" -> defaultValue.getDatetimeValue() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_DATETIME,
                    null,
                    null,
                    null,
                    defaultValue.getDatetimeValue(),
                    null,
                    null,
                    null,
                    0
            ));
            case "user" -> defaultValue.getUserValueId() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_USER,
                    null,
                    null,
                    null,
                    null,
                    defaultValue.getUserValueId(),
                    null,
                    null,
                    0
            ));
            case "group" -> defaultValue.getGroupValueId() == null
                    ? List.of()
                    : List.of(buildCustomFieldValue(
                    customField.getId(),
                    contextId,
                    VALUE_TYPE_GROUP,
                    null,
                    null,
                    null,
                    null,
                    null,
                    defaultValue.getGroupValueId(),
                    null,
                    0
            ));
            case "select" -> {
                if (defaultValue.getOptionValueId() == null) {
                    yield List.of();
                }
                CustomFieldOptionEntity option = resolveOption(defaultValue.getOptionValueId(), options, fieldKey);
                yield List.of(buildCustomFieldValue(
                        customField.getId(),
                        contextId,
                        VALUE_TYPE_OPTION,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        option.getId(),
                        0
                ));
            }
            default -> extractFallbackJsonValue(defaultValue) == null
                    ? List.of()
                    : List.of(buildJsonCustomFieldValue(
                    customField.getId(),
                    contextId,
                    extractFallbackJsonValue(defaultValue),
                    0
            ));
        };
    }

    private String extractFallbackJsonValue(CustomFieldContextDefaultValueEntity defaultValue) {
        if (defaultValue.getJsonValue() != null) {
            return defaultValue.getJsonValue();
        }
        if (defaultValue.getTextValue() != null) {
            return jsonUtils.toJson(defaultValue.getTextValue());
        }
        if (defaultValue.getNumberValue() != null) {
            return jsonUtils.toJson(defaultValue.getNumberValue());
        }
        if (defaultValue.getDateValue() != null) {
            return jsonUtils.toJson(defaultValue.getDateValue());
        }
        if (defaultValue.getDatetimeValue() != null) {
            return jsonUtils.toJson(defaultValue.getDatetimeValue());
        }
        if (defaultValue.getUserValueId() != null) {
            return jsonUtils.toJson(defaultValue.getUserValueId());
        }
        if (defaultValue.getGroupValueId() != null) {
            return jsonUtils.toJson(defaultValue.getGroupValueId());
        }
        if (defaultValue.getOptionValueId() != null) {
            return jsonUtils.toJson(defaultValue.getOptionValueId());
        }
        return null;
    }

    private WorkItemCustomFieldValueEntity buildJsonCustomFieldValue(Long customFieldId,
                                                                     Long contextId,
                                                                     String jsonValue,
                                                                     Integer sortOrder) {
        return WorkItemCustomFieldValueEntity.builder()
                .customFieldId(customFieldId)
                .customFieldContextId(contextId)
                .valueType(VALUE_TYPE_JSON)
                .jsonValue(jsonValue)
                .sortOrder(sortOrder)
                .build();
    }

    private WorkItemCustomFieldValueEntity buildCustomFieldValue(Long customFieldId,
                                                                 Long contextId,
                                                                 String valueType,
                                                                 String textValue,
                                                                 BigDecimal numberValue,
                                                                 Long dateValue,
                                                                 Long datetimeValue,
                                                                 Long userValueId,
                                                                 String groupValueId,
                                                                 Long optionValueId,
                                                                 Integer sortOrder) {
        return WorkItemCustomFieldValueEntity.builder()
                .customFieldId(customFieldId)
                .customFieldContextId(contextId)
                .valueType(valueType)
                .textValue(textValue)
                .numberValue(numberValue)
                .dateValue(dateValue)
                .datetimeValue(datetimeValue)
                .userValueId(userValueId)
                .groupValueId(groupValueId)
                .optionValueId(optionValueId)
                .sortOrder(sortOrder)
                .build();
    }

    private String requireTextValue(Object rawValue, String fieldKey) {
        if (rawValue instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires text value: field=" + fieldKey
        );
    }

    private BigDecimal requireNumberValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return new BigDecimal(number.toString());
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                return new BigDecimal(text.trim());
            }
        } catch (NumberFormatException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires numeric value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires numeric value: field=" + fieldKey
        );
    }

    private Long requireUserValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                return Long.parseLong(text.trim());
            }
        } catch (NumberFormatException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires user id value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires user id value: field=" + fieldKey
        );
    }

    private String requireGroupValue(Object rawValue, String fieldKey) {
        if (rawValue instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires group value: field=" + fieldKey
        );
    }

    private Long requireDateValue(Object rawValue, String fieldKey) {
        try {
            LocalDate localDate;
            if (rawValue instanceof Number number) {
                localDate = Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (rawValue instanceof String text && !text.isBlank()) {
                try {
                    localDate = LocalDate.parse(text.trim());
                } catch (DateTimeParseException exception) {
                    localDate = Instant.parse(text.trim()).atZone(ZoneId.systemDefault()).toLocalDate();
                }
            } else {
                throw new DateTimeParseException("Unsupported date format", String.valueOf(rawValue), 0);
            }
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires date value: field=" + fieldKey
            );
        }
    }

    private Long requireDateTimeValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                try {
                    return Instant.parse(text.trim()).toEpochMilli();
                } catch (DateTimeParseException exception) {
                    return LocalDateTime.parse(text.trim())
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                }
            }
        } catch (DateTimeParseException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires datetime value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires datetime value: field=" + fieldKey
        );
    }

    private List<?> asList(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }
        if (rawValue instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        return List.of(rawValue);
    }

    private CustomFieldOptionEntity resolveOption(Object rawValue,
                                                  List<CustomFieldOptionEntity> options,
                                                  String fieldKey) {
        String textCandidate = rawValue instanceof String text ? text.trim() : null;
        Long numericCandidate = null;

        if (rawValue instanceof Number number) {
            numericCandidate = number.longValue();
        } else if (textCandidate != null && !textCandidate.isBlank()) {
            try {
                numericCandidate = Long.parseLong(textCandidate);
            } catch (NumberFormatException ignored) {
                numericCandidate = null;
            }
        }

        for (CustomFieldOptionEntity option : options) {
            if (Boolean.TRUE.equals(option.getIsDisabled())) {
                continue;
            }
            if (textCandidate != null && !textCandidate.isBlank() && textCandidate.equals(option.getOptionKey())) {
                return option;
            }
            if (numericCandidate != null && numericCandidate.equals(option.getId())) {
                return option;
            }
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field option is invalid: field=" + fieldKey + ", value=" + rawValue
        );
    }

    private void validateRequiredFields(CreateWorkItemRequest request,
                                        Long priorityId,
                                        Long assigneeId,
                                        Long securityLevelId,
                                        CreateFieldRules createFieldRules,
                                        ResolvedCustomFields resolvedCustomFields) {
        List<String> missingFields = new ArrayList<>();

        Map<String, Object> effectiveSystemValues = new LinkedHashMap<>();
        effectiveSystemValues.put(SYSTEM_FIELD_ISSUE_TYPE_ID, request.getIssueTypeId());
        effectiveSystemValues.put(SYSTEM_FIELD_SUMMARY, request.getSummary());
        effectiveSystemValues.put(SYSTEM_FIELD_DESCRIPTION, request.getDescription());
        effectiveSystemValues.put(SYSTEM_FIELD_PRIORITY_ID, priorityId);
        effectiveSystemValues.put(SYSTEM_FIELD_ASSIGNEE_ID, assigneeId);
        effectiveSystemValues.put(SYSTEM_FIELD_PARENT_ID, request.getParentId());
        effectiveSystemValues.put(SYSTEM_FIELD_DUE_DATE, request.getDueDate());
        effectiveSystemValues.put(SYSTEM_FIELD_TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate());
        effectiveSystemValues.put(SYSTEM_FIELD_SECURITY_LEVEL_ID, securityLevelId);

        for (FieldPolicy systemPolicy : createFieldRules.systemPolicies().values()) {
            if (!systemPolicy.required() || !SUPPORTED_SYSTEM_FIELDS.contains(systemPolicy.fieldRef())) {
                continue;
            }
            if (isMissingValue(effectiveSystemValues.get(systemPolicy.fieldRef()))) {
                missingFields.add(systemPolicy.fieldRef());
            }
        }

        missingFields.addAll(resolvedCustomFields.missingFields());

        if (!missingFields.isEmpty()) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.REQUIRED_FIELDS_MISSING,
                    "Required fields are missing: " + String.join(", ", missingFields)
            );
        }
    }

    private boolean isMissingValue(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof String text && text.isBlank();
    }

    private void persistCustomFieldValues(Long workItemId,
                                          List<WorkItemCustomFieldValueEntity> values,
                                          Long tenantId,
                                          Long userId) {
        if (values == null || values.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (WorkItemCustomFieldValueEntity value : values) {
            value.setWorkItemId(workItemId);
            value.setTenantId(tenantId);
            value.setCreatedAt(now);
            value.setCreatedBy(userId);
            value.setUpdatedAt(now);
            value.setUpdatedBy(userId);
        }

        workItemCustomFieldValuePort.saveAll(values);
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void validateIssueTypeInProjectScheme(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getIssueTypeSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                    "Project has no issue type scheme binding: projectId=" + project.getId()
            );
        }

        boolean exists = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(
                        project.getIssueTypeSchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .anyMatch(issueTypeId::equals);

        if (!exists) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME,
                    "Issue type is not allowed in project scheme: projectId=" + project.getId() + ", issueTypeId=" + issueTypeId
            );
        }
    }

    private void validateParentRequirement(IssueTypeEntity issueType, Long parentId) {
        if (issueType.getHierarchyLevel() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue type hierarchy level is missing: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() == 0 && parentId == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Subtask issue type requires parent_id: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() >= 2 && parentId != null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue types with hierarchy level >= 2 cannot set parent_id: issueTypeId=" + issueType.getId()
                            + ", hierarchyLevel=" + issueType.getHierarchyLevel()
            );
        }
    }

    private WorkflowStepEntity resolveInitialWorkflowStep(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getWorkflowSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                    "Project has no workflow scheme binding: projectId=" + project.getId()
            );
        }

        WorkflowSchemeEntity workflowScheme = workflowSchemePort
                .getWorkflowSchemeById(project.getWorkflowSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found: id=" + project.getWorkflowSchemeId()
                ));

        Long workflowId = workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(
                        project.getWorkflowSchemeId(),
                        tenantId
                )
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .findFirst()
                .orElse(workflowScheme.getDefaultWorkflowId());

        if (workflowId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING,
                    "Workflow scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflowId));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow has no published version: workflowId=" + workflowId
            );
        }

        WorkflowVersionEntity publishedVersion = workflowVersionPort
                .getWorkflowVersionById(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                        "Published workflow version not found: id=" + workflow.getCurrentPublishedVersionId()
                ));

        if (!publishedVersion.isActive()) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow current version is not published: workflowId=" + workflowId
                            + ", versionId=" + publishedVersion.getId()
            );
        }

        return workflowStepPort.getInitialStepByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NO_INITIAL_STEP,
                        "Workflow must have exactly one initial step: workflowId=" + workflowId
                ));
    }

    private Long resolvePriorityId(ProjectEntity project, Long requestedPriorityId, Long tenantId) {
        if (project.getPrioritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + project.getId()
            );
        }

        PrioritySchemeEntity priorityScheme = prioritySchemePort
                .getPrioritySchemeById(project.getPrioritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + project.getPrioritySchemeId()
                ));

        List<PrioritySchemeItemEntity> priorityItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeId(project.getPrioritySchemeId(), tenantId);

        if (requestedPriorityId != null) {
            boolean inScheme = priorityItems.stream()
                    .map(PrioritySchemeItemEntity::getPriorityId)
                    .anyMatch(requestedPriorityId::equals);
            if (!inScheme) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.PRIORITY_NOT_IN_SCHEME,
                        "Priority is not allowed in project scheme: projectId=" + project.getId() + ", priorityId=" + requestedPriorityId
                );
            }
            return requestedPriorityId;
        }

        if (priorityScheme.getDefaultPriorityId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.DEFAULT_PRIORITY_NOT_CONFIGURED,
                    "Priority scheme has no default priority: schemeId=" + project.getPrioritySchemeId()
            );
        }

        return priorityScheme.getDefaultPriorityId();
    }

    private Long resolveAssigneeId(ProjectEntity project,
                                   Long requestedAssigneeId,
                                   ProjectPermissionEvaluationContext actorContext,
                                   Long tenantId) {
        if (requestedAssigneeId == null) {
            return null;
        }

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.ASSIGN_ISSUES);

        ProjectPermissionEvaluationContext assigneeContext = ProjectPermissionEvaluationContext.builder()
                .userId(requestedAssigneeId)
                .build();

        if (!projectPermissionEvaluationService.hasPermission(project, assigneeContext, ProjectPermissionKeys.ASSIGNABLE_USER)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Assignee is not assignable in project: projectId=" + project.getId() + ", assigneeId=" + requestedAssigneeId
            );
        }

        return requestedAssigneeId;
    }

    private Long resolveSecurityLevelId(ProjectEntity project,
                                        Long requestedSecurityLevelId,
                                        ProjectPermissionEvaluationContext actorContext,
                                        Long tenantId) {
        if (project.getIssueSecuritySchemeId() == null) {
            return requestedSecurityLevelId == null ? null : missingIssueSecurityScheme(project.getId());
        }

        IssueSecuritySchemeEntity issueSecurityScheme = issueSecuritySchemePort
                .getIssueSecuritySchemeById(project.getIssueSecuritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + project.getIssueSecuritySchemeId()
                ));

        if (requestedSecurityLevelId == null) {
            return issueSecurityScheme.getDefaultLevelId();
        }

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);

        boolean inScheme = issueSecurityLevelPort.getIssueSecurityLevelsBySchemeId(
                        project.getIssueSecuritySchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueSecurityLevelEntity::getId)
                .anyMatch(requestedSecurityLevelId::equals);

        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in project scheme: projectId=" + project.getId()
                            + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
    }

    private Long missingIssueSecurityScheme(Long projectId) {
        throw new ResourceNotFoundException(
                DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                "Project has no issue security scheme binding: projectId=" + projectId
        );
    }

    private void persistCreatedOutboxEvent(WorkItemEntity workItem,
                                           Long tenantId,
                                           Long userId,
                                           Long projectId) {
        WorkItemEventPayload payload = WorkItemEventPayload.builder()
                .workItemId(workItem.getId())
                .workItemKey(workItem.getKey())
                .projectId(projectId)
                .issueTypeId(workItem.getIssueTypeId())
                .statusId(workItem.getStatusId())
                .assigneeId(workItem.getAssigneeId())
                .build();

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.WorkItem.AGGREGATE)
                .aggregateId(workItem.getId())
                .eventType(EventConstants.WorkItem.EventType.WORK_ITEM_CREATED)
                .topic(EventConstants.WorkItem.TOPIC)
                .partitionKey(String.valueOf(projectId))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        outboxEventService.saveEvent(outboxEvent);
    }

    private record FieldRef(String fieldRefType, String fieldRef) {
    }

    private record FieldPolicy(String fieldRefType,
                               String fieldRef,
                               boolean required,
                               boolean hidden,
                               boolean onCreateScreen) {
    }

    private record CreateFieldRules(Map<String, FieldPolicy> systemPolicies,
                                    Map<String, FieldPolicy> customPolicies) {
    }

    private record ResolvedCustomFields(List<WorkItemCustomFieldValueEntity> values,
                                        List<String> missingFields) {
    }
}
