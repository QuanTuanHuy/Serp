/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCalendarSettingsModelMappingTest {

    @Test
    void modelsShouldKeepCalendarSettingsFields() {
        ResourceCalendarProfileModel profile = ResourceCalendarProfileModel.builder()
                .tenantId(10L)
                .name("VN Full-time")
                .description("Default Vietnam office calendar")
                .timezone("Asia/Ho_Chi_Minh")
                .isDefault(true)
                .build();

        ResourceCalendarProfileBlockModel block = ResourceCalendarProfileBlockModel.builder()
                .profileId(1L)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .capacityFactor(new BigDecimal("0.75"))
                .build();

        ResourceCalendarAssignmentModel assignment = ResourceCalendarAssignmentModel.builder()
                .tenantId(10L)
                .userId(20L)
                .profileId(1L)
                .effectiveFrom(LocalDate.of(2026, 6, 7))
                .effectiveTo(null)
                .build();

        ResourceCalendarExceptionModel exception = ResourceCalendarExceptionModel.builder()
                .tenantId(10L)
                .userId(20L)
                .exceptionType("CAPACITY_OVERRIDE")
                .startAt(LocalDateTime.of(2026, 6, 8, 9, 0))
                .endAt(LocalDateTime.of(2026, 6, 8, 12, 0))
                .capacityFactor(new BigDecimal("0.50"))
                .reason("Training")
                .build();

        assertThat(profile.getTimezone()).isEqualTo("Asia/Ho_Chi_Minh");
        assertThat(block.getCapacityFactor()).isEqualByComparingTo("0.75");
        assertThat(assignment.getEffectiveTo()).isNull();
        assertThat(exception.getExceptionType()).isEqualTo("CAPACITY_OVERRIDE");
    }
}
