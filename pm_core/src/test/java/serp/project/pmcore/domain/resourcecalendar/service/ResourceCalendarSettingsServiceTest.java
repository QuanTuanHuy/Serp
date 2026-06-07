/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.service.impl.ResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceCalendarSettingsServiceTest {

    private final ResourceCalendarSettingsService service = new ResourceCalendarSettingsService();

    @Test
    void validateBlocksShouldRejectInvalidRangeAndCapacity() {
        ResourceCalendarProfileBlockEntity block = ResourceCalendarProfileBlockEntity.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(9, 0))
                .capacityFactor(BigDecimal.ONE)
                .build();

        assertThatThrownBy(() -> service.validateBlocks(List.of(block)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void validateExceptionShouldAllowUnavailableWithoutCapacityFactor() {
        ResourceCalendarExceptionEntity exception = ResourceCalendarExceptionEntity.builder()
                .exceptionType(ResourceCalendarExceptionType.UNAVAILABLE)
                .startAt(LocalDateTime.of(2026, 6, 8, 9, 0))
                .endAt(LocalDateTime.of(2026, 6, 8, 17, 0))
                .capacityFactor(null)
                .build();

        assertThatCode(() -> service.validateException(exception)).doesNotThrowAnyException();
    }

    @Test
    void validateAssignmentsShouldRejectOverlapForSameUser() {
        List<ResourceCalendarAssignmentEntity> assignments = List.of(
                ResourceCalendarAssignmentEntity.builder()
                        .userId(20L)
                        .profileId(1L)
                        .effectiveFrom(LocalDate.of(2026, 6, 1))
                        .effectiveTo(LocalDate.of(2026, 6, 30))
                        .build(),
                ResourceCalendarAssignmentEntity.builder()
                        .userId(20L)
                        .profileId(2L)
                        .effectiveFrom(LocalDate.of(2026, 6, 15))
                        .effectiveTo(null)
                        .build()
        );

        assertThatThrownBy(() -> service.validateAssignments(assignments))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
