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
import serp.project.school_bus_service.enums.DemoEventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_demo_event_log")
@Getter
@Setter
public class DemoEventLogEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "demo_session_id")
    private DemoSessionEntity demoSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private DemoEventType eventType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;
}

