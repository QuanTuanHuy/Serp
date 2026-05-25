package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.EventSource;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_attendance")
@Getter
@Setter
public class AttendanceEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private TripExecutionEntity trip;

    @ManyToOne
    @JoinColumn(name = "route_stop_id")
    private RouteStopEntity routeStop;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type", nullable = false)
    private AttendanceType attendanceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private AttendanceEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", nullable = false)
    private EventSource eventSource = EventSource.MANUAL;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "recorded_by", nullable = false)
    private Long recordedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
