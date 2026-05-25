package com.example.ttcrs.entity;

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
    name = "distances",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"src_code", "dest_code"})
    }
)
public class DistanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "src_code", nullable = false, length = 50)
    private String srcCode;

    @Column(name = "dest_code", nullable = false, length = 50)
    private String destCode;

    /** Khoảng cách tính bằng km */
    @Column(name = "distance", nullable = false)
    private Double distance;

    /** Thời gian di chuyển tính bằng giây */
    @Column(name = "travel_time", nullable = false)
    private Double travelTime;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;
}
