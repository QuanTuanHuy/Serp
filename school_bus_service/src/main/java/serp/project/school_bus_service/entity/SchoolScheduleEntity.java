package serp.project.school_bus_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_bus_school_schedule")
@Getter
@Setter
public class SchoolScheduleEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @Column(name = "schedule_code")
    private String scheduleCode;

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "grade")
    private String grade;

    @Column(name = "shift_type", nullable = false)
    private String shiftType;

    @Column(name = "arrival_deadline")
    private LocalTime arrivalDeadline;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefaultSchedule = Boolean.FALSE;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<SchoolScheduleDayEntity> scheduleDays = new ArrayList<>();
}

