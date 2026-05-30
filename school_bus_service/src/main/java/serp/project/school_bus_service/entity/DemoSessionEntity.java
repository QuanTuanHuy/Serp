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
import serp.project.school_bus_service.enums.DemoSessionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_demo_session")
@Getter
@Setter
public class DemoSessionEntity extends BaseModel {

    @Column(name = "demo_code")
    private String demoCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id")
    private TripExecutionEntity trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemoSessionStatus status;

    @Column(name = "speed_multiplier", nullable = false)
    private Integer speedMultiplier = 1;

    @Column(name = "current_stop_order")
    private Integer currentStopOrder;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "progress_percent", nullable = false)
    private Double progressPercent = 0D;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "auto_advance_stops", nullable = false)
    private Boolean autoAdvanceStops = Boolean.FALSE;

    @Column(name = "auto_attendance", nullable = false)
    private Boolean autoAttendance = Boolean.FALSE;

    @Column(name = "last_tick_at")
    private LocalDateTime lastTickAt;

    @Column(name = "last_event_type", length = 50)
    private String lastEventType;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

