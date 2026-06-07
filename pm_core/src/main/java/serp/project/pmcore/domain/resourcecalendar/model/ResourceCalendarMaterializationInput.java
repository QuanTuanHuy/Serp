/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.model;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;

import java.time.LocalDate;
import java.util.List;

public record ResourceCalendarMaterializationInput(
        Long tenantId,
        List<Long> userIds,
        String timezone,
        LocalDate windowStart,
        LocalDate windowEnd,
        List<ResourceCalendarProfileBlockEntity> blocks,
        List<ResourceCalendarExceptionEntity> exceptions
) {
}
