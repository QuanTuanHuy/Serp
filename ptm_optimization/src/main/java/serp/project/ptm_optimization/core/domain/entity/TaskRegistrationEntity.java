/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaskRegistrationEntity extends BaseEntity {
    private Long userId;
    private String name;
    private Double maxWorkTime;
    private Double constant1;
    private Double constant2;
    private Double constant3;

    private Double sleepDuration;
    private String startSleepTime;
    private String endSleepTime;
    private Double relaxTime;
    private Double travelTime;
    private Double eatTime;
    private Double workTime;

    private Integer status;
}
