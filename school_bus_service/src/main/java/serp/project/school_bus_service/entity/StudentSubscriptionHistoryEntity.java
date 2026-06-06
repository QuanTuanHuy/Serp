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
import serp.project.school_bus_service.enums.SubscriptionChangeType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_student_subscription_history")
@Getter
@Setter
public class StudentSubscriptionHistoryEntity extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @ManyToOne
    @JoinColumn(name = "source_request_id")
    private TransportRequestEntity sourceRequest;

    @ManyToOne
    @JoinColumn(name = "request_student_id")
    private RequestStudentEntity requestStudent;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private SubscriptionChangeType changeType;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "old_pickup_point_id")
    private Long oldPickupPointId;

    @Column(name = "new_pickup_point_id")
    private Long newPickupPointId;

    @Column(name = "old_dropoff_point_id")
    private Long oldDropoffPointId;

    @Column(name = "new_dropoff_point_id")
    private Long newDropoffPointId;

    @Column(name = "old_school_schedule_id")
    private Long oldSchoolScheduleId;

    @Column(name = "new_school_schedule_id")
    private Long newSchoolScheduleId;

    @Column(name = "old_trip_option")
    private String oldTripOption;

    @Column(name = "new_trip_option")
    private String newTripOption;

    @Column(name = "old_effective_from")
    private LocalDate oldEffectiveFrom;

    @Column(name = "new_effective_from")
    private LocalDate newEffectiveFrom;

    @Column(name = "old_effective_to")
    private LocalDate oldEffectiveTo;

    @Column(name = "new_effective_to")
    private LocalDate newEffectiveTo;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
