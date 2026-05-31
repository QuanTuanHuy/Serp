package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "school_bus_school_pickup_point_window")
@Getter
@Setter
public class SchoolPickupPointWindowEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_pickup_point_id")
    private SchoolPickupPointEntity schoolPickupPoint;

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_schedule_id")
    private SchoolScheduleEntity schoolSchedule;

    @Column(name = "direction", nullable = false)
    private String direction;

    @Column(name = "window_start", nullable = false)
    private LocalTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalTime windowEnd;

    @Column(name = "estimated_distance_to_school_km")
    private Double estimatedDistanceToSchoolKm;

    @Column(name = "estimated_duration_to_school_min")
    private Integer estimatedDurationToSchoolMin;
}
