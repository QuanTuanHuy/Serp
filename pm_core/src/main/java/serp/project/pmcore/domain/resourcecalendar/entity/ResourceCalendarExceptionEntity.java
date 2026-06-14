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
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCalendarExceptionEntity extends BaseEntity {
    private Long tenantId;
    private Long userId;
    private ResourceCalendarExceptionType exceptionType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal capacityFactor;
    private String reason;
}
