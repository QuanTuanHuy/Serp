/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.settings;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public record ResourceCalendarSettingsOverviewView(
        List<ProfileView> profiles,
        List<AssignmentView> assignments,
        List<ExceptionView> upcomingExceptions,
        List<Long> unassignedUserIds,
        Long materializedWindowStart,
        Long materializedWindowEnd,
        Long fetchedAt
) {
    public record ProfileView(
            Long id,
            Long tenantId,
            String name,
            String description,
            String timezone,
            boolean isDefault,
            int assignmentCount,
            List<BlockView> blocks
    ) {
        public static ProfileView from(ResourceCalendarProfileEntity profile,
                                       List<ResourceCalendarProfileBlockEntity> blocks,
                                       int assignmentCount) {
            return new ProfileView(
                    profile.getId(),
                    profile.getTenantId(),
                    profile.getName(),
                    profile.getDescription(),
                    profile.getTimezone(),
                    Boolean.TRUE.equals(profile.getIsDefault()),
                    assignmentCount,
                    blocks == null ? List.of() : blocks.stream().map(BlockView::from).toList()
            );
        }
    }

    public record BlockView(
            Long id,
            Long profileId,
            Integer dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal capacityFactor
    ) {
        public static BlockView from(ResourceCalendarProfileBlockEntity block) {
            return new BlockView(
                    block.getId(),
                    block.getProfileId(),
                    block.getDayOfWeek(),
                    block.getStartTime(),
                    block.getEndTime(),
                    block.getCapacityFactor()
            );
        }
    }

    public record AssignmentView(
            Long id,
            Long userId,
            Long profileId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        public static AssignmentView from(ResourceCalendarAssignmentEntity assignment) {
            return new AssignmentView(
                    assignment.getId(),
                    assignment.getUserId(),
                    assignment.getProfileId(),
                    assignment.getEffectiveFrom(),
                    assignment.getEffectiveTo()
            );
        }
    }

    public record ExceptionView(
            Long id,
            Long userId,
            ResourceCalendarExceptionType exceptionType,
            Long startAt,
            Long endAt,
            BigDecimal capacityFactor,
            String reason
    ) {
        public static ExceptionView from(ResourceCalendarExceptionEntity exception) {
            return new ExceptionView(
                    exception.getId(),
                    exception.getUserId(),
                    exception.getExceptionType(),
                    toEpochMillis(exception.getStartAt()),
                    toEpochMillis(exception.getEndAt()),
                    exception.getCapacityFactor(),
                    exception.getReason()
            );
        }
    }

    private static Long toEpochMillis(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
