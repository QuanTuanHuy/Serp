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
import serp.project.school_bus_service.enums.PausePeriodStatus;

import java.time.LocalDate;

@Entity
@Table(name = "school_bus_subscription_pause_period")
@Getter
@Setter
public class SubscriptionPausePeriodEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @ManyToOne
    @JoinColumn(name = "source_request_id")
    private TransportRequestEntity sourceRequest;

    @ManyToOne
    @JoinColumn(name = "request_student_id")
    private RequestStudentEntity requestStudent;

    @Column(name = "pause_from", nullable = false)
    private LocalDate pauseFrom;

    @Column(name = "pause_to")
    private LocalDate pauseTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PausePeriodStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
