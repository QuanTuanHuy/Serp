/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPrioritySettingsOverviewQueryHandler
        implements IQueryHandler<GetPrioritySettingsOverviewQuery, PrioritySettingsOverviewView> {

    private static final int SETTINGS_OVERVIEW_LIMIT = 100;

    private final IPriorityService priorityService;
    private final IPrioritySchemeService prioritySchemeService;
    private final IProjectReadPort projectReadPort;

    @Override
    @Transactional(readOnly = true)
    public PrioritySettingsOverviewView handle(GetPrioritySettingsOverviewQuery query) {
        List<PriorityEntity> priorities = listVisiblePriorities(query.tenantId());
        List<PrioritySchemeEntity> schemes = listVisiblePrioritySchemes(query.tenantId());
        List<PrioritySchemeEntity> schemeDetails = schemes.stream()
                .map(scheme -> prioritySchemeService.getVisiblePrioritySchemeDetailById(
                        scheme.getId(),
                        query.tenantId()
                ))
                .toList();

        Map<Long, PriorityEntity> prioritiesById = priorities.stream()
                .collect(Collectors.toMap(PriorityEntity::getId, priority -> priority));
        Map<Long, List<PrioritySettingsOverviewView.SchemeRefView>> schemeRefsByPriorityId =
                buildSchemeRefsByPriorityId(schemeDetails);
        Map<Long, List<PrioritySettingsOverviewView.ProjectRefView>> projectRefsBySchemeId =
                buildProjectRefsBySchemeId(query.tenantId(), schemes);

        return new PrioritySettingsOverviewView(
                priorities.stream()
                        .map(priority -> toPriorityView(priority, schemeRefsByPriorityId))
                        .toList(),
                schemeDetails.stream()
                        .map(scheme -> toPrioritySchemeView(scheme, prioritiesById, projectRefsBySchemeId))
                        .toList()
        );
    }

    private List<PriorityEntity> listVisiblePriorities(Long tenantId) {
        PriorityListCriteria criteria = PriorityListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .sortBy("sequence")
                .sortDirection("ASC")
                .build();
        return priorityService.listVisiblePriorities(tenantId, criteria).items();
    }

    private List<PrioritySchemeEntity> listVisiblePrioritySchemes(Long tenantId) {
        PrioritySchemeListCriteria criteria = PrioritySchemeListCriteria.builder()
                .page(0)
                .pageSize(SETTINGS_OVERVIEW_LIMIT)
                .sortBy("name")
                .sortDirection("ASC")
                .build();
        return prioritySchemeService.listVisiblePrioritySchemes(tenantId, criteria).items();
    }

    private Map<Long, List<PrioritySettingsOverviewView.SchemeRefView>> buildSchemeRefsByPriorityId(
            List<PrioritySchemeEntity> schemes) {
        Map<Long, List<PrioritySettingsOverviewView.SchemeRefView>> refsByPriorityId = new LinkedHashMap<>();
        for (PrioritySchemeEntity scheme : schemes) {
            PrioritySettingsOverviewView.SchemeRefView schemeRef =
                    new PrioritySettingsOverviewView.SchemeRefView(
                            scheme.getId(),
                            scheme.getName(),
                            scheme.isSystem()
                    );
            for (PrioritySchemeItemEntity item : safeItems(scheme)) {
                refsByPriorityId.computeIfAbsent(item.getPriorityId(), ignored -> new ArrayList<>())
                        .add(schemeRef);
            }
        }
        return refsByPriorityId;
    }

    private Map<Long, List<PrioritySettingsOverviewView.ProjectRefView>> buildProjectRefsBySchemeId(
            Long tenantId,
            List<PrioritySchemeEntity> schemes) {
        List<Long> schemeIds = schemes.stream()
                .map(PrioritySchemeEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (schemeIds.isEmpty()) {
            return Map.of();
        }

        return projectReadPort.getActiveProjectsByPrioritySchemeIds(schemeIds, tenantId).stream()
                .filter(project -> project.getPrioritySchemeId() != null)
                .collect(Collectors.groupingBy(
                        ProjectEntity::getPrioritySchemeId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                project -> new PrioritySettingsOverviewView.ProjectRefView(
                                        project.getId(),
                                        project.getKey(),
                                        project.getName()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private PrioritySettingsOverviewView.PriorityView toPriorityView(
            PriorityEntity priority,
            Map<Long, List<PrioritySettingsOverviewView.SchemeRefView>> schemeRefsByPriorityId) {
        return new PrioritySettingsOverviewView.PriorityView(
                priority.getId(),
                priority.getTenantId(),
                priority.getPriorityKey(),
                priority.getName(),
                priority.getDescription(),
                priority.getIconUrl(),
                priority.getColor(),
                priority.getSequence(),
                priority.isSystem(),
                priority.isSystem(),
                schemeRefsByPriorityId.getOrDefault(priority.getId(), List.of()),
                priority.getCreatedAt(),
                priority.getCreatedBy(),
                priority.getUpdatedAt(),
                priority.getUpdatedBy()
        );
    }

    private PrioritySettingsOverviewView.PrioritySchemeView toPrioritySchemeView(
            PrioritySchemeEntity scheme,
            Map<Long, PriorityEntity> prioritiesById,
            Map<Long, List<PrioritySettingsOverviewView.ProjectRefView>> projectRefsBySchemeId) {
        List<PrioritySettingsOverviewView.PriorityOptionView> priorities = safeItems(scheme).stream()
                .sorted(Comparator
                        .comparing((PrioritySchemeItemEntity item) ->
                                item.getSequence() == null ? Integer.MAX_VALUE : item.getSequence())
                        .thenComparing(PrioritySchemeItemEntity::getPriorityId))
                .map(item -> toPriorityOptionView(
                        item,
                        prioritiesById.get(item.getPriorityId()),
                        Objects.equals(item.getPriorityId(), scheme.getDefaultPriorityId())
                ))
                .filter(Objects::nonNull)
                .toList();

        return new PrioritySettingsOverviewView.PrioritySchemeView(
                scheme.getId(),
                scheme.getTenantId(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getDefaultPriorityId(),
                scheme.isSystem(),
                scheme.isSystem(),
                priorities,
                projectRefsBySchemeId.getOrDefault(scheme.getId(), List.of()),
                scheme.getCreatedAt(),
                scheme.getCreatedBy(),
                scheme.getUpdatedAt(),
                scheme.getUpdatedBy()
        );
    }

    private PrioritySettingsOverviewView.PriorityOptionView toPriorityOptionView(
            PrioritySchemeItemEntity item,
            PriorityEntity priority,
            boolean isDefault) {
        if (priority == null) {
            return null;
        }
        return new PrioritySettingsOverviewView.PriorityOptionView(
                priority.getId(),
                priority.getPriorityKey(),
                priority.getName(),
                priority.getDescription(),
                priority.getIconUrl(),
                priority.getColor(),
                item.getSequence(),
                isDefault
        );
    }

    private List<PrioritySchemeItemEntity> safeItems(PrioritySchemeEntity scheme) {
        return scheme.getItems() == null ? List.of() : scheme.getItems();
    }
}
