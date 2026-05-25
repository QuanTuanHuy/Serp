/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.RepTimeBlockType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RepTimeBlockEntity extends BaseEntity {
    private Long teamMemberId;
    private Long activityId;
    private Long startTime;
    private Long endTime;
    private RepTimeBlockType blockType;
    private Long version;
}
