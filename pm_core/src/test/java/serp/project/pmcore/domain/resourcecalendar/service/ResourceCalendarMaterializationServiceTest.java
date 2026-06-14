/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;
import serp.project.pmcore.domain.resourcecalendar.service.impl.ResourceCalendarMaterializationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCalendarMaterializationServiceTest {

    private final ResourceCalendarMaterializationService service = new ResourceCalendarMaterializationService();

    @Test
    void materializeShouldGenerateWeeklyBlocksWithCapacityFactor() {
        ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
                10L,
                List.of(20L),
                "Asia/Ho_Chi_Minh",
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 8),
                List.of(block(1, 9, 17, "0.50")),
                List.of()
        );

        var slots = service.materialize(input);

        assertThat(slots).hasSize(1);
        assertThat(slots.getFirst().capacityMillis()).isEqualTo(4L * 60 * 60 * 1000);
    }

    @Test
    void materializeShouldRemoveUnavailableOverlap() {
        ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
                10L,
                List.of(20L),
                "Asia/Ho_Chi_Minh",
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 8),
                List.of(block(1, 9, 17, "1.00")),
                List.of(ResourceCalendarExceptionEntity.builder()
                        .tenantId(10L)
                        .userId(20L)
                        .exceptionType(ResourceCalendarExceptionType.UNAVAILABLE)
                        .startAt(LocalDateTime.of(2026, 6, 8, 12, 0))
                        .endAt(LocalDateTime.of(2026, 6, 8, 13, 0))
                        .build())
        );

        var slots = service.materialize(input);

        assertThat(slots).hasSize(2);
        assertThat(slots.stream().map(slot -> slot.capacityMillis()).toList())
                .containsExactly(3L * 60 * 60 * 1000, 4L * 60 * 60 * 1000);
    }

    @Test
    void materializeShouldApplyCapacityOverrideOverlap() {
        ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
                10L,
                List.of(20L),
                "Asia/Ho_Chi_Minh",
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 8),
                List.of(block(1, 9, 17, "1.00")),
                List.of(ResourceCalendarExceptionEntity.builder()
                        .tenantId(10L)
                        .userId(20L)
                        .exceptionType(ResourceCalendarExceptionType.CAPACITY_OVERRIDE)
                        .startAt(LocalDateTime.of(2026, 6, 8, 13, 0))
                        .endAt(LocalDateTime.of(2026, 6, 8, 17, 0))
                        .capacityFactor(new BigDecimal("0.50"))
                        .build())
        );

        var slots = service.materialize(input);

        assertThat(slots).hasSize(2);
        assertThat(slots.stream().map(slot -> slot.capacityMillis()).toList())
                .containsExactly(4L * 60 * 60 * 1000, 2L * 60 * 60 * 1000);
    }

    private ResourceCalendarProfileBlockEntity block(int day, int startHour, int endHour, String factor) {
        return ResourceCalendarProfileBlockEntity.builder()
                .dayOfWeek(day)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .capacityFactor(new BigDecimal(factor))
                .build();
    }
}
