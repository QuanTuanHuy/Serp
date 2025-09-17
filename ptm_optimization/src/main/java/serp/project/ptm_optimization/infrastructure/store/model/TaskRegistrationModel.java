/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.ptm_optimization.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "task_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaskRegistrationModel extends BaseModel {
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "name")
    private String name;
    @Column(name = "max_work_time")
    private Double maxWorkTime;
    @Column(name = "constant_1")
    private Double constant1;
    @Column(name = "constant_2")
    private Double constant2;
    @Column(name = "constant_3")
    private Double constant3;

    @Column(name = "sleep_duration")
    private Double sleepDuration;
    @Column(name = "start_sleep_time")
    private String startSleepTime;
    @Column(name = "end_sleep_time")
    private String endSleepTime;
    @Column(name = "relax_time")
    private Double relaxTime;
    @Column(name = "travel_time")
    private Double travelTime;
    @Column(name = "eat_time")
    private Double eatTime;
    @Column(name = "work_time")
    private Double workTime;

    @Column(name = "status")
    private Integer status;
}
