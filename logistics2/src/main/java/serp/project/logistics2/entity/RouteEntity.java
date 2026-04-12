package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_route")
public class RouteEntity {

    @Id
    private String id;

    @Column(name = "delivery_plan_id")
    private String deliveryPlanId;

    @Column(name = "vehicle_shipper_id")
    private String vehicleShipperId;

    @Column(name = "total_distance_km")
    private Long totalDistanceKm;

    @Column(name = "total_weight_loaded_kg")
    private Long TotalWeightLoadedKg;

    @Column(name = "total_volume_loaded_cbm")
    private Long TotalVolumeLoadedCbm;

    private String status;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

}
