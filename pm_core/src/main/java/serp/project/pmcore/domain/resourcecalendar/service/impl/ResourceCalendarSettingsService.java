/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceCalendarSettingsService implements IResourceCalendarSettingsService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWO = new BigDecimal("2.00");

    @Override
    public void validateBlocks(List<ResourceCalendarProfileBlockEntity> blocks) {
        for (ResourceCalendarProfileBlockEntity block : safeList(blocks)) {
            if (block.getDayOfWeek() == null || block.getDayOfWeek() < 1 || block.getDayOfWeek() > 7) {
                throw violation("Calendar block day must be between 1 and 7");
            }
            if (block.getStartTime() == null || block.getEndTime() == null
                    || !block.getStartTime().isBefore(block.getEndTime())) {
                throw violation("Calendar block start time must be before end time");
            }
            BigDecimal factor = block.getCapacityFactor();
            if (factor == null || factor.compareTo(ZERO) <= 0 || factor.compareTo(ONE) > 0) {
                throw violation("Calendar block capacity factor must be greater than 0 and no more than 1");
            }
        }
    }

    @Override
    public void validateException(ResourceCalendarExceptionEntity exception) {
        if (exception == null) {
            throw violation("Calendar exception is required");
        }
        if (exception.getStartAt() == null || exception.getEndAt() == null
                || !exception.getStartAt().isBefore(exception.getEndAt())) {
            throw violation("Calendar exception start must be before end");
        }
        if (exception.getExceptionType() == ResourceCalendarExceptionType.CAPACITY_OVERRIDE) {
            BigDecimal factor = exception.getCapacityFactor();
            if (factor == null || factor.compareTo(ZERO) < 0 || factor.compareTo(TWO) > 0) {
                throw violation("Capacity override factor must be from 0 through 2");
            }
        }
    }

    @Override
    public void validateAssignments(List<ResourceCalendarAssignmentEntity> assignments) {
        var byUserId = safeList(assignments).stream()
                .filter(assignment -> assignment.getUserId() != null)
                .collect(Collectors.groupingBy(ResourceCalendarAssignmentEntity::getUserId));
        for (List<ResourceCalendarAssignmentEntity> userAssignments : byUserId.values()) {
            List<ResourceCalendarAssignmentEntity> ordered = userAssignments.stream()
                    .sorted(Comparator.comparing(ResourceCalendarAssignmentEntity::getEffectiveFrom))
                    .toList();
            for (int index = 1; index < ordered.size(); index++) {
                ResourceCalendarAssignmentEntity previous = ordered.get(index - 1);
                ResourceCalendarAssignmentEntity current = ordered.get(index);
                if (previous.getEffectiveTo() == null || !previous.getEffectiveTo().isBefore(current.getEffectiveFrom())) {
                    throw violation("A user cannot have overlapping active calendar assignments");
                }
            }
        }
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? List.of() : items;
    }

    private BusinessRuleViolationException violation(String message) {
        return new BusinessRuleViolationException(DomainErrorCode.BAD_REQUEST, message);
    }
}
