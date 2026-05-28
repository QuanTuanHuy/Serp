/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetIssueTypeSettingsOverviewQueryHandler
        implements IQueryHandler<GetIssueTypeSettingsOverviewQuery, IssueTypeSettingsOverviewView> {

    private static final int SETTINGS_OVERVIEW_LIMIT = 100;

    private final IIssueTypeService issueTypeService;
    private final IIssueTypeSchemeService issueTypeSchemeService;
    private final IProjectReadPort projectReadPort;

    @Override
    @Transactional(readOnly = true)
    public IssueTypeSettingsOverviewView handle(GetIssueTypeSettingsOverviewQuery query) {
        List<IssueTypeEntity> issueTypes = listVisibleIssueTypes(query.tenantId()).stream()
                .filter(issueType -> !issueType.isSystem())
                .toList();
        List<IssueTypeSchemeEntity> schemes = listVisibleIssueTypeSchemes(query.tenantId()).stream()
                .filter(scheme -> !scheme.isSystem())
                .toList();
        List<IssueTypeSchemeEntity> schemeDetails = schemes.stream()
                .map(scheme -> issueTypeSchemeService.getVisibleIssueTypeSchemeDetailById(
                        scheme.getId(),
                        query.tenantId()
                ))
                .toList();

        Map<Long, IssueTypeEntity> issueTypesById = issueTypes.stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, issueType -> issueType));
        Map<Long, List<IssueTypeSettingsOverviewView.SchemeRefView>> schemeRefsByIssueTypeId =
                buildSchemeRefsByIssueTypeId(schemeDetails);
        Map<Long, List<IssueTypeSettingsOverviewView.ProjectRefView>> projectRefsBySchemeId =
                buildProjectRefsBySchemeId(query.tenantId(), schemes);

        return new IssueTypeSettingsOverviewView(
                issueTypes.stream()
                        .map(issueType -> toWorkTypeView(issueType, schemeRefsByIssueTypeId))
                        .toList(),
                schemeDetails.stream()
                        .map(scheme -> toWorkTypeSchemeView(scheme, issueTypesById, projectRefsBySchemeId))
                        .toList()
        );
    }

    private List<IssueTypeEntity> listVisibleIssueTypes(Long tenantId) {
        IssueTypeListCriteria criteria = IssueTypeListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .isSystem(false)
                .sortBy("hierarchy_level")
                .sortDirection("ASC")
                .build();
        return issueTypeService.listVisibleIssueTypes(tenantId, criteria).items();
    }

    private List<IssueTypeSchemeEntity> listVisibleIssueTypeSchemes(Long tenantId) {
        IssueTypeSchemeListCriteria criteria = IssueTypeSchemeListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .isSystem(false)
                .sortBy("name")
                .sortDirection("ASC")
                .build();
        return issueTypeSchemeService.listVisibleIssueTypeSchemes(tenantId, criteria).items();
    }

    private Map<Long, List<IssueTypeSettingsOverviewView.SchemeRefView>> buildSchemeRefsByIssueTypeId(
            List<IssueTypeSchemeEntity> schemes) {
        Map<Long, List<IssueTypeSettingsOverviewView.SchemeRefView>> refsByIssueTypeId = new LinkedHashMap<>();
        for (IssueTypeSchemeEntity scheme : schemes) {
            IssueTypeSettingsOverviewView.SchemeRefView schemeRef =
                    new IssueTypeSettingsOverviewView.SchemeRefView(
                            scheme.getId(),
                            scheme.getName(),
                            scheme.isSystem()
                    );
            for (IssueTypeSchemeItemEntity item : safeItems(scheme)) {
                refsByIssueTypeId.computeIfAbsent(item.getIssueTypeId(), ignored -> new java.util.ArrayList<>())
                        .add(schemeRef);
            }
        }
        return refsByIssueTypeId;
    }

    private Map<Long, List<IssueTypeSettingsOverviewView.ProjectRefView>> buildProjectRefsBySchemeId(
            Long tenantId,
            List<IssueTypeSchemeEntity> schemes) {
        List<Long> schemeIds = schemes.stream()
                .map(IssueTypeSchemeEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (schemeIds.isEmpty()) {
            return Map.of();
        }

        return projectReadPort.getActiveProjectsByIssueTypeSchemeIds(schemeIds, tenantId).stream()
                .filter(project -> project.getIssueTypeSchemeId() != null)
                .collect(Collectors.groupingBy(
                        ProjectEntity::getIssueTypeSchemeId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                project -> new IssueTypeSettingsOverviewView.ProjectRefView(
                                        project.getId(),
                                        project.getKey(),
                                        project.getName()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private IssueTypeSettingsOverviewView.WorkTypeView toWorkTypeView(
            IssueTypeEntity issueType,
            Map<Long, List<IssueTypeSettingsOverviewView.SchemeRefView>> schemeRefsByIssueTypeId) {
        return new IssueTypeSettingsOverviewView.WorkTypeView(
                issueType.getId(),
                issueType.getTenantId(),
                issueType.getTypeKey(),
                issueType.getName(),
                issueType.getDescription(),
                issueType.getIconUrl(),
                issueType.getHierarchyLevel(),
                issueType.isSystem(),
                issueType.isSystem(),
                schemeRefsByIssueTypeId.getOrDefault(issueType.getId(), List.of()),
                issueType.getCreatedAt(),
                issueType.getCreatedBy(),
                issueType.getUpdatedAt(),
                issueType.getUpdatedBy()
        );
    }

    private IssueTypeSettingsOverviewView.WorkTypeSchemeView toWorkTypeSchemeView(
            IssueTypeSchemeEntity scheme,
            Map<Long, IssueTypeEntity> issueTypesById,
            Map<Long, List<IssueTypeSettingsOverviewView.ProjectRefView>> projectRefsBySchemeId) {
        List<IssueTypeSettingsOverviewView.WorkTypeOptionView> workTypes = safeItems(scheme).stream()
                .sorted(Comparator
                        .comparing((IssueTypeSchemeItemEntity item) ->
                                item.getSequence() == null ? Integer.MAX_VALUE : item.getSequence())
                        .thenComparing(IssueTypeSchemeItemEntity::getIssueTypeId))
                .map(item -> toWorkTypeOptionView(item, issueTypesById.get(item.getIssueTypeId()),
                        Objects.equals(item.getIssueTypeId(), scheme.getDefaultIssueTypeId())))
                .filter(Objects::nonNull)
                .toList();

        return new IssueTypeSettingsOverviewView.WorkTypeSchemeView(
                scheme.getId(),
                scheme.getTenantId(),
                scheme.getName(),
                scheme.getDescription(),
                issueTypesById.containsKey(scheme.getDefaultIssueTypeId()) ? scheme.getDefaultIssueTypeId() : null,
                scheme.isSystem(),
                scheme.isSystem(),
                workTypes,
                projectRefsBySchemeId.getOrDefault(scheme.getId(), List.of()),
                scheme.getCreatedAt(),
                scheme.getCreatedBy(),
                scheme.getUpdatedAt(),
                scheme.getUpdatedBy()
        );
    }

    private IssueTypeSettingsOverviewView.WorkTypeOptionView toWorkTypeOptionView(
            IssueTypeSchemeItemEntity item,
            IssueTypeEntity issueType,
            boolean isDefault) {
        if (issueType == null) {
            return null;
        }
        return new IssueTypeSettingsOverviewView.WorkTypeOptionView(
                issueType.getId(),
                issueType.getTypeKey(),
                issueType.getName(),
                issueType.getDescription(),
                issueType.getIconUrl(),
                issueType.getHierarchyLevel(),
                item.getSequence(),
                isDefault
        );
    }

    private List<IssueTypeSchemeItemEntity> safeItems(IssueTypeSchemeEntity scheme) {
        return scheme.getItems() == null ? List.of() : scheme.getItems();
    }
}
