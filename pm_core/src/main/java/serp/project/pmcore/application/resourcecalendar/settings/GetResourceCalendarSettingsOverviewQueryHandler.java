/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfileBlockPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetResourceCalendarSettingsOverviewQueryHandler
        implements IQueryHandler<GetResourceCalendarSettingsOverviewQuery, ResourceCalendarSettingsOverviewView> {
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarProfileBlockPort blockPort;
    private final IResourceCalendarAssignmentPort assignmentPort;
    private final IResourceCalendarExceptionPort exceptionPort;

    @Override
    @Transactional(readOnly = true)
    public ResourceCalendarSettingsOverviewView handle(GetResourceCalendarSettingsOverviewQuery query) {
        List<ResourceCalendarProfileEntity> profiles = profilePort.listProfiles(query.tenantId());
        List<ResourceCalendarAssignmentEntity> assignments = assignmentPort.listActiveAssignments(query.tenantId());
        Map<Long, List<ResourceCalendarAssignmentEntity>> assignmentsByProfileId = assignments.stream()
                .collect(Collectors.groupingBy(ResourceCalendarAssignmentEntity::getProfileId));
        Map<Long, List<ResourceCalendarProfileBlockEntity>> blocksByProfileId = profiles.stream()
                .collect(Collectors.toMap(ResourceCalendarProfileEntity::getId, profile -> blockPort.listByProfileId(profile.getId())));

        LocalDate windowStart = LocalDate.now(ZoneOffset.UTC);
        LocalDate windowEnd = windowStart.plusDays(90);
        long windowStartMillis = windowStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long windowEndMillis = windowEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        List<Long> userIds = assignments.stream()
                .map(ResourceCalendarAssignmentEntity::getUserId)
                .distinct()
                .toList();
        List<ResourceCalendarExceptionEntity> exceptions = exceptionPort.listExceptions(
                query.tenantId(),
                userIds,
                windowStartMillis,
                windowEndMillis
        );

        return new ResourceCalendarSettingsOverviewView(
                profiles.stream()
                        .map(profile -> ResourceCalendarSettingsOverviewView.ProfileView.from(
                                profile,
                                blocksByProfileId.getOrDefault(profile.getId(), List.of()),
                                assignmentsByProfileId.getOrDefault(profile.getId(), List.of()).size()
                        ))
                        .toList(),
                assignments.stream().map(ResourceCalendarSettingsOverviewView.AssignmentView::from).toList(),
                exceptions.stream().map(ResourceCalendarSettingsOverviewView.ExceptionView::from).toList(),
                List.of(),
                windowStartMillis,
                windowEndMillis,
                System.currentTimeMillis()
        );
    }
}
