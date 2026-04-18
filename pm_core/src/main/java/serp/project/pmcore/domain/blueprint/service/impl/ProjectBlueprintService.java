/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;
import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.port.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.domain.blueprint.port.IProjectBlueprintPort;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectBlueprintService implements IProjectBlueprintService {

    private static final Set<String> VALID_PROJECT_TYPES = Set.of("software", "business", "service_desk");
    private static final int BLUEPRINT_NAME_MAX_LENGTH = 255;
    private static final int BLUEPRINT_DESCRIPTION_MAX_LENGTH = 2000;
    private static final int BLUEPRINT_AVATAR_URL_MAX_LENGTH = 255;

    private final IProjectBlueprintPort projectBlueprintPort;
    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;

    @Override
    public ProjectBlueprintEntity createBlueprint(ProjectBlueprintEntity blueprint, Long tenantId, Long userId) {
        String name = TextNormalizationUtils.normalizeRequiredText(
                blueprint.getName(),
                "name",
                BLUEPRINT_NAME_MAX_LENGTH
        );
        if (projectBlueprintPort.existsByNameAndTenantId(name, tenantId)) {
            log.warn("Project blueprint name already exists: name={}, tenantId={}", name, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.BLUEPRINT_NAME_ALREADY_EXISTS);
        }

        String typeKey = normalizeProjectType(blueprint.getTypeKey());
        blueprint.setTenantId(tenantId);
        blueprint.setName(name);
        blueprint.setDescription(TextNormalizationUtils.normalizeOptionalText(
                blueprint.getDescription(),
                BLUEPRINT_DESCRIPTION_MAX_LENGTH,
                "description"
        ));
        blueprint.setTypeKey(typeKey);
        blueprint.setAvatarUrl(TextNormalizationUtils.normalizeOptionalText(
                blueprint.getAvatarUrl(),
                BLUEPRINT_AVATAR_URL_MAX_LENGTH,
                "avatarUrl"
        ));
        blueprint.setIsSystem(false);
        blueprint.setDeletedAt(null);
        blueprint.applyCreate(userId, System.currentTimeMillis());
        return projectBlueprintPort.saveBlueprint(blueprint);
    }

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId) {
        return projectBlueprintPort.getBlueprintByIdIncludingSystem(blueprintId, tenantId);
    }

    @Override
    public ProjectBlueprintEntity getBlueprintByIdIncludingSystemOrThrow(Long blueprintId, Long tenantId) {
        return projectBlueprintPort.getBlueprintByIdIncludingSystem(blueprintId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.blueprint(blueprintId));
    }

    private ProjectBlueprintEntity getBlueprintByIdOrThrow(Long blueprintId, Long tenantId) {
        return projectBlueprintPort.getBlueprintById(blueprintId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.blueprint(blueprintId));
    }

    @Override
    public List<BlueprintSchemeDefaultEntity> getBlueprintDefaultsIncludingSystem(Long blueprintId, Long tenantId) {
        return blueprintSchemeDefaultPort.getDefaultsByBlueprintIdIncludingSystem(blueprintId, tenantId);
    }

    @Override
    public PageResult<ProjectBlueprintEntity> listBlueprintsIncludingSystem(Long tenantId, ProjectBlueprintListCriteria criteria) {
        String projectTypeKey = criteria.getProjectTypeKey();
        if (projectTypeKey != null) {
            normalizeProjectType(projectTypeKey);
        }
        return projectBlueprintPort.listBlueprintsIncludingSystem(tenantId, criteria);
    }

    @Override
    public ProjectBlueprintEntity updateBlueprint(Long blueprintId, ProjectBlueprintUpdateData data, Long tenantId, Long userId) {
        ProjectBlueprintEntity existing = getBlueprintByIdOrThrow(blueprintId, tenantId);
        ensureWritable(existing);

        if (data.nameProvided()) {
            String newName = TextNormalizationUtils.normalizeRequiredText(
                    data.name(),
                    "name",
                    BLUEPRINT_NAME_MAX_LENGTH
            );
            if (!newName.equalsIgnoreCase(existing.getName())
                    && projectBlueprintPort.existsByNameAndTenantId(newName, tenantId)) {
                throw new BusinessRuleViolationException(DomainErrorCode.BLUEPRINT_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(TextNormalizationUtils.normalizeOptionalText(
                    data.description(),
                    BLUEPRINT_DESCRIPTION_MAX_LENGTH,
                    "description"
            ));
        }

        if (data.avatarUrlProvided()) {
            existing.setAvatarUrl(TextNormalizationUtils.normalizeOptionalText(
                    data.avatarUrl(),
                    BLUEPRINT_AVATAR_URL_MAX_LENGTH,
                    "avatarUrl"
            ));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        return projectBlueprintPort.saveBlueprint(existing);
    }

    @Override
    public ProjectBlueprintEntity deleteBlueprint(Long blueprintId, Long tenantId, Long userId) {
        ProjectBlueprintEntity existing = getBlueprintByIdOrThrow(blueprintId, tenantId);
        ensureWritable(existing);

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        return projectBlueprintPort.saveBlueprint(existing);
    }

    private void ensureWritable(ProjectBlueprintEntity blueprint) {
        if (blueprint.isSystem()) {
            throw new BusinessRuleViolationException(DomainErrorCode.BLUEPRINT_IS_SYSTEM);
        }
    }

    private String normalizeProjectType(String value) {
        String trimmed = TextNormalizationUtils.normalizeRequiredText(value, "projectTypeKey", 50);
        if (!VALID_PROJECT_TYPES.contains(trimmed)) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_TYPE_INVALID);
        }
        return trimmed;
    }

}
