/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "working_hours", indexes = {
        @Index(name = "idx_working_hours_team_member_id", columnList = "team_member_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_working_hours_member_day", columnNames = {"team_member_id", "day_of_week"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkingHoursModel extends BaseModel {

    @Column(name = "team_member_id", nullable = false)
    private Long teamMemberId;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek;

    @Column(name = "working_day", nullable = false)
    private Boolean workingDay;

    @Column(name = "start_minute")
    private Integer startMinute;

    @Column(name = "end_minute")
    private Integer endMinute;
}
