package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.RouteStatus;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Float totalDistanceKm;

    @Column(name = "total_weight_loaded_kg")
    private Long totalWeightLoadedKg;

    @Column(name = "total_volume_loaded_cbm")
    private Double totalVolumeLoadedCbm;

    private String status;

    @Column(name = "route_stop_count")
    private int routeStopCount;

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

    @Transient
    private List<RouteStopEntity> routeStops = new ArrayList<>();

    @Transient
    private VehicleShipperEntity vehicleShipper;

    public RouteEntity(String id, String deliveryPlanId, String vehicleShipperId, Float totalDistanceKm,
            Long totalWeightLoadedKg, Double totalVolumeLoadedCbm, String status, int routeStopCount,
            LocalDate deliveryDate, Long tenantId) {
        this.id = id;
        this.deliveryPlanId = deliveryPlanId;
        this.vehicleShipperId = vehicleShipperId;
        this.totalDistanceKm = totalDistanceKm;
        this.totalWeightLoadedKg = totalWeightLoadedKg;
        this.totalVolumeLoadedCbm = totalVolumeLoadedCbm;
        this.status = status;
        this.routeStopCount = routeStopCount;
        this.deliveryDate = deliveryDate;
        this.tenantId = tenantId;
    }

    public static RouteEntity create(
            String deliveryPlanId,
            String vehicleShipperId,
            Float totalDistanceKm,
            Long totalWeightLoadedKg,
            Double totalVolumeLoadedCbm,
            LocalDate deliveryDate,
            Long tenantId,
            List<RouteStopEntity> routeStops) {
        String id = IdUtils.generateRouteId();
        String status = RouteStatus.PENDING.name();
        int routeStopCount = routeStops != null ? routeStops.size() : 0;
        RouteEntity route = new RouteEntity(id, deliveryPlanId, vehicleShipperId, totalDistanceKm, totalWeightLoadedKg,
                totalVolumeLoadedCbm, status, routeStopCount, deliveryDate, tenantId);
        if (routeStops != null) {
            routeStops.forEach(stop -> stop.setRouteId(id));
            route.setRouteStops(routeStops);
        }
        return route;
    }
}
