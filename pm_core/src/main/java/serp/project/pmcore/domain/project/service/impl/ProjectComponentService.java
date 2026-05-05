/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectComponentPort;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectComponentService implements IProjectComponentService {

    private static final int COMPONENT_NAME_MAX_LENGTH = 255;
    private static final int COMPONENT_DESCRIPTION_MAX_LENGTH = 2000;
    private static final String DEFAULT_ASSIGNEE_TYPE = "PROJECT_DEFAULT";
    private static final Set<String> ALLOWED_ASSIGNEE_TYPES = Set.of(
            "PROJECT_DEFAULT",
            "COMPONENT_LEAD",
            "PROJECT_LEAD",
            "UNASSIGNED"
    );

    private final IProjectComponentPort projectComponentPort;
    private final IProjectService projectService;
    private final ProjectComponentLeadValidator projectComponentLeadValidator;

    @Override
    public ProjectComponentEntity createComponent(ProjectComponentEntity component, Long tenantId, Long userId) {
        ProjectEntity project = getWritableProject(component.getProjectId(), tenantId);
        String normalizedName = normalizeName(component.getName());
        ensureUniqueName(component.getProjectId(), tenantId, normalizedName, null);

        Long leadUserId = normalizeLeadUserId(component.getLeadUserId());
        projectComponentLeadValidator.validateLeadUserExists(leadUserId);

        component.setTenantId(tenantId);
        component.setProjectId(project.getId());
        component.setName(normalizedName);
        component.setDescription(normalizeDescription(component.getDescription()));
        component.setLeadUserId(leadUserId);
        component.setAssigneeType(normalizeAssigneeType(component.getAssigneeType()));
        component.setDeletedAt(null);
        component.applyCreate(userId, System.currentTimeMillis());

        return projectComponentPort.createComponent(component);
    }

    @Override
    public ProjectComponentEntity getComponentById(Long componentId, Long projectId, Long tenantId) {
        ensureProjectExists(projectId, tenantId);
        return projectComponentPort.getComponentById(componentId, projectId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Project component not found: componentId={}, projectId={}, tenantId={}",
                            componentId, projectId, tenantId);
                    return ResourceNotFoundException.component(componentId);
                });
    }

    @Override
    public java.util.List<ProjectComponentEntity> getComponentsByIds(java.util.List<Long> componentIds,
                                                                     Long projectId,
                                                                     Long tenantId) {
        ensureProjectExists(projectId, tenantId);
        if (componentIds == null || componentIds.isEmpty()) {
            return java.util.List.of();
        }

        java.util.List<ProjectComponentEntity> components =
                projectComponentPort.getComponentsByIds(componentIds, projectId, tenantId);
        Map<Long, ProjectComponentEntity> componentsById = components.stream()
                .collect(Collectors.toMap(ProjectComponentEntity::getId, Function.identity()));

        for (Long componentId : componentIds) {
            if (!componentsById.containsKey(componentId)) {
                log.warn("Project component not found: componentId={}, projectId={}, tenantId={}",
                        componentId, projectId, tenantId);
                throw ResourceNotFoundException.component(componentId);
            }
        }

        return componentIds.stream()
                .map(componentsById::get)
                .toList();
    }

    @Override
    public PageResult<ProjectComponentEntity> listComponents(Long projectId,
                                                             Long tenantId,
                                                             ProjectComponentListCriteria criteria) {
        ensureProjectExists(projectId, tenantId);
        return projectComponentPort.listComponents(projectId, tenantId, criteria);
    }

    @Override
    public ProjectComponentEntity updateComponent(Long componentId,
                                                  Long projectId,
                                                  ProjectComponentUpdateData data,
                                                  Long tenantId,
                                                  Long userId) {
        ensureAtLeastOneMutableField(data);
        getWritableProject(projectId, tenantId);
        ProjectComponentEntity existing = getComponentById(componentId, projectId, tenantId);

        if (data.nameProvided()) {
            String newName = normalizeName(data.name());
            ensureUniqueName(projectId, tenantId, newName, existing.getName());
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(normalizeDescription(data.description()));
        }

        if (data.leadUserIdProvided()) {
            Long leadUserId = normalizeLeadUserId(data.leadUserId());
            if (leadUserId != null && !leadUserId.equals(existing.getLeadUserId())) {
                projectComponentLeadValidator.validateLeadUserExists(leadUserId);
            }
            existing.setLeadUserId(leadUserId);
        }

        if (data.assigneeTypeProvided()) {
            existing.setAssigneeType(normalizeAssigneeType(data.assigneeType()));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        projectComponentPort.updateComponent(existing);
        return existing;
    }

    @Override
    public ProjectComponentEntity deleteComponent(Long componentId, Long projectId, Long tenantId, Long userId) {
        getWritableProject(projectId, tenantId);
        ProjectComponentEntity existing = getComponentById(componentId, projectId, tenantId);

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        projectComponentPort.updateComponent(existing);
        projectComponentPort.deleteComponentLinks(componentId, tenantId);
        return existing;
    }

    private void ensureAtLeastOneMutableField(ProjectComponentUpdateData data) {
        if (!data.nameProvided()
                && !data.descriptionProvided()
                && !data.leadUserIdProvided()
                && !data.assigneeTypeProvided()) {
            throw new IllegalArgumentException("At least one mutable component field must be provided");
        }
    }

    private void ensureProjectExists(Long projectId, Long tenantId) {
        projectService.getProjectById(projectId, tenantId);
    }

    private ProjectEntity getWritableProject(Long projectId, Long tenantId) {
        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
        return project;
    }

    private void ensureUniqueName(Long projectId, Long tenantId, String newName, String existingName) {
        if (existingName != null && newName.equalsIgnoreCase(existingName)) {
            return;
        }
        if (projectComponentPort.existsByProjectIdAndName(projectId, tenantId, newName)) {
            throw new BusinessRuleViolationException(DomainErrorCode.COMPONENT_NAME_ALREADY_EXISTS);
        }
    }

    private String normalizeName(String rawName) {
        return TextNormalizationUtils.normalizeRequiredText(rawName, "name", COMPONENT_NAME_MAX_LENGTH);
    }

    private String normalizeDescription(String rawDescription) {
        return TextNormalizationUtils.normalizeOptionalText(
                rawDescription,
                COMPONENT_DESCRIPTION_MAX_LENGTH,
                "description"
        );
    }

    private Long normalizeLeadUserId(Long leadUserId) {
        return leadUserId;
    }

    private String normalizeAssigneeType(String rawAssigneeType) {
        String normalized = rawAssigneeType == null || rawAssigneeType.isBlank()
                ? DEFAULT_ASSIGNEE_TYPE
                : rawAssigneeType.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ASSIGNEE_TYPES.contains(normalized)) {
            throw new DomainValidationException(
                    DomainErrorCode.BAD_REQUEST,
                    "assigneeType must be one of PROJECT_DEFAULT, COMPONENT_LEAD, PROJECT_LEAD, UNASSIGNED"
            );
        }
        return normalized;
    }
}
