package com.example.ttcrs.entity;

import com.example.ttcrs.constant.StopAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(
    name = "transport_plan_stops",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "transport_plan_stops_transport_plan_id_sequence_unique",
            columnNames = {"transport_plan_id", "sequence"}
        )
    },
    indexes = {
        @Index(name = "transport_plan_stops_transport_plan_id_index", columnList = "transport_plan_id"),
        @Index(name = "transport_plan_stops_request_id_index", columnList = "request_id")
    }
)
public class TransportPlanStopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "transport_plan_id", nullable = false)
    private Long transportPlanId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "location_code", nullable = false, length = 50)
    private String locationCode;

    /** ID của request liên quan (null nếu là điểm depot) */
    @Column(name = "request_id")
    private Long requestId;

    /** ID của rơ-moóc dùng tại điểm này (null nếu không có) */
    @Column(name = "trailer_id")
    private Long trailerId;

    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private StopAction action;

    @Column(name = "planned_arrival_time")
    private LocalDateTime plannedArrivalTime;

    /** Được cập nhật khi tài xế check-in thực tế */
    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;
}
