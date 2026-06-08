/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;
import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarMaterializationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ResourceCalendarMaterializationService implements IResourceCalendarMaterializationService {

    @Override
    public List<GeneratedResourceCalendarSlot> materialize(ResourceCalendarMaterializationInput input) {
        if (input == null || input.tenantId() == null || input.userIds() == null || input.userIds().isEmpty()
                || input.windowStart() == null || input.windowEnd() == null || input.windowStart().isAfter(input.windowEnd())) {
            return List.of();
        }
        ZoneId zoneId = ZoneId.of(input.timezone() == null || input.timezone().isBlank() ? "UTC" : input.timezone());
        List<ResourceCalendarProfileBlockEntity> blocks = input.blocks() == null ? List.of() : input.blocks();
        List<ResourceCalendarExceptionEntity> exceptions = input.exceptions() == null ? List.of() : input.exceptions();
        List<GeneratedResourceCalendarSlot> slots = new ArrayList<>();

        for (Long userId : input.userIds().stream().filter(Objects::nonNull).distinct().toList()) {
            List<ResourceCalendarExceptionEntity> userExceptions = exceptions.stream()
                    .filter(exception -> Objects.equals(exception.getUserId(), userId))
                    .sorted(Comparator.comparing(ResourceCalendarExceptionEntity::getStartAt))
                    .toList();
            for (LocalDate day = input.windowStart(); !day.isAfter(input.windowEnd()); day = day.plusDays(1)) {
                int dayOfWeek = day.getDayOfWeek().getValue();
                LocalDate currentDay = day;
                for (ResourceCalendarProfileBlockEntity block : blocks) {
                    if (!Objects.equals(block.getDayOfWeek(), dayOfWeek)) {
                        continue;
                    }
                    LocalDateTime blockStart = currentDay.atTime(block.getStartTime());
                    LocalDateTime blockEnd = currentDay.atTime(block.getEndTime());
                    List<Segment> segments = new ArrayList<>();
                    segments.add(new Segment(blockStart, blockEnd, block.getCapacityFactor(), ResourceCalendarSlotSource.PROFILE));
                    for (ResourceCalendarExceptionEntity exception : userExceptions) {
                        segments = applyException(segments, exception);
                    }
                    for (Segment segment : segments) {
                        long capacityMillis = capacityMillis(segment, zoneId);
                        if (capacityMillis <= 0) {
                            continue;
                        }
                        slots.add(new GeneratedResourceCalendarSlot(
                                input.tenantId(),
                                userId,
                                toEpochMillis(segment.start(), zoneId),
                                toEpochMillis(segment.end(), zoneId),
                                capacityMillis,
                                segment.source(),
                                null
                        ));
                    }
                }
            }
        }

        slots.sort(Comparator.comparing(GeneratedResourceCalendarSlot::userId)
                .thenComparing(GeneratedResourceCalendarSlot::slotStart));
        return slots;
    }

    private List<Segment> applyException(List<Segment> segments, ResourceCalendarExceptionEntity exception) {
        if (exception.getStartAt() == null || exception.getEndAt() == null
                || !exception.getStartAt().isBefore(exception.getEndAt())) {
            return segments;
        }
        List<Segment> result = new ArrayList<>();
        for (Segment segment : segments) {
            if (!overlaps(segment, exception)) {
                result.add(segment);
                continue;
            }
            LocalDateTime overlapStart = max(segment.start(), exception.getStartAt());
            LocalDateTime overlapEnd = min(segment.end(), exception.getEndAt());
            if (segment.start().isBefore(overlapStart)) {
                result.add(new Segment(segment.start(), overlapStart, segment.factor(), segment.source()));
            }
            if (exception.getExceptionType() == ResourceCalendarExceptionType.CAPACITY_OVERRIDE) {
                result.add(new Segment(
                        overlapStart,
                        overlapEnd,
                        exception.getCapacityFactor() == null ? BigDecimal.ZERO : exception.getCapacityFactor(),
                        ResourceCalendarSlotSource.EXCEPTION
                ));
            }
            if (overlapEnd.isBefore(segment.end())) {
                result.add(new Segment(overlapEnd, segment.end(), segment.factor(), segment.source()));
            }
        }
        return result;
    }

    private boolean overlaps(Segment segment, ResourceCalendarExceptionEntity exception) {
        return segment.start().isBefore(exception.getEndAt()) && segment.end().isAfter(exception.getStartAt());
    }

    private LocalDateTime max(LocalDateTime left, LocalDateTime right) {
        return left.isAfter(right) ? left : right;
    }

    private LocalDateTime min(LocalDateTime left, LocalDateTime right) {
        return left.isBefore(right) ? left : right;
    }

    private long capacityMillis(Segment segment, ZoneId zoneId) {
        long durationMillis = toEpochMillis(segment.end(), zoneId) - toEpochMillis(segment.start(), zoneId);
        return BigDecimal.valueOf(durationMillis)
                .multiply(segment.factor())
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private long toEpochMillis(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    private record Segment(
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal factor,
            ResourceCalendarSlotSource source
    ) {
    }
}
