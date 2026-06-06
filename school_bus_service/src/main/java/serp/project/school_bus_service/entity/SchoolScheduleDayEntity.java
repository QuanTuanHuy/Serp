package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_school_schedule_day")
@Getter
@Setter
public class SchoolScheduleDayEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_schedule_id")
    private SchoolScheduleEntity schedule;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;
}
