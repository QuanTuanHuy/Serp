/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCalendarProfileBlockEntity extends BaseEntity {
    private Long profileId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal capacityFactor;
}
