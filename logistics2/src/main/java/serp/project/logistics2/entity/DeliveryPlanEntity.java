package serp.project.logistics2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.PlanOptimizationStatus;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_delivery_plan")
public class DeliveryPlanEntity {

        @Id
        private String id;

        @Column(name = "facility_id")
        private String facilityId;

        @Column(name = "plan_code")
        private String planCode;

        @Column(name = "delivery_date")
        private LocalDate deliveryDate;

        @Column(name = "optimization_status")
        private String optimizationStatus;

        @Column(name = "total_slips")
        private int totalSlips;

        @Column(name = "total_vehicles")
        private int totalVehicles;

        @Column(name = "created_by_user_id")
        private Long createdByUserId;

        @CreationTimestamp
        @Column(name = "created_stamp")
        private LocalDateTime createdStamp;

        @UpdateTimestamp
        @Column(name = "last_updated_stamp")
        private LocalDateTime lastUpdatedStamp;

        @Column(name = "tenant_id")
        private Long tenantId;

        @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
        @JoinTable(name = "wms2_delivery_plan_slip", joinColumns = @JoinColumn(name = "delivery_plan_id"), inverseJoinColumns = @JoinColumn(name = "delivery_slip_id"))
        private Set<DeliverySlipEntity> slips = new HashSet<>();

        @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
        @JoinTable(name = "wms2_delivery_plan_vehicle_shipper", joinColumns = @JoinColumn(name = "delivery_plan_id"), inverseJoinColumns = @JoinColumn(name = "vehicle_shipper_id"))
        private Set<VehicleShipperEntity> vehicleShippers = new HashSet<>();

        @Transient
        private FacilityEntity facility;

        public DeliveryPlanEntity(
                        String id,
                        String planCode,
                        LocalDate deliveryDate,
                        String optimizationStatus,
                        int totalSlips,
                        int totalVehicles,
                        Long createdByUserId,
                        Long tenantId,
                        List<DeliverySlipEntity> slips,
                        List<VehicleShipperEntity> vehicleShippers) {
                this.id = id;
                this.planCode = planCode;
                this.deliveryDate = deliveryDate;
                this.optimizationStatus = optimizationStatus;
                this.totalSlips = totalSlips;
                this.totalVehicles = totalVehicles;
                this.createdByUserId = createdByUserId;
                this.tenantId = tenantId;
                this.slips = new HashSet<>(slips);
                this.vehicleShippers = new HashSet<>(vehicleShippers);
        }

        public static DeliveryPlanEntity create(
                        LocalDate deliveryDate,
                        Long createdByUserId,
                        Long tenantId,
                        List<DeliverySlipEntity> slips,
                        List<VehicleShipperEntity> vehicleShippers) {
                String id = IdUtils.generateDeliveryPlanId();
                String planCode = "PL-" + id.substring(6, 12);
                String optimizationStatus = PlanOptimizationStatus.DRAFT.name();
                int totalSlips = slips.size();
                int totalVehicles = vehicleShippers.size();
                return new DeliveryPlanEntity(
                                id,
                                planCode,
                                deliveryDate,
                                optimizationStatus,
                                totalSlips,
                                totalVehicles,
                                createdByUserId,
                                tenantId,
                                slips,
                                vehicleShippers);
        }

        public void addSlip(DeliverySlipEntity slip) {
                if (PlanOptimizationStatus.valueOf(this.optimizationStatus)
                                .ordinal() >= PlanOptimizationStatus.OPTIMIZING.ordinal()) {
                        throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
                }

                this.slips.add(slip);
                this.totalSlips = this.slips.size();
        }

        public void removeSlip(DeliverySlipEntity slip) {
                if (PlanOptimizationStatus.valueOf(this.optimizationStatus)
                                .ordinal() >= PlanOptimizationStatus.OPTIMIZING.ordinal()) {
                        throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
                }

                this.slips.remove(slip);
                this.totalSlips = this.slips.size();
        }

        public void addVehicleShipper(VehicleShipperEntity vehicleShipper) {
                if (PlanOptimizationStatus.valueOf(this.optimizationStatus)
                                .ordinal() >= PlanOptimizationStatus.OPTIMIZING.ordinal()) {
                        throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
                }

                this.vehicleShippers.add(vehicleShipper);
                this.totalVehicles = this.vehicleShippers.size();
        }

        public void removeVehicleShipper(VehicleShipperEntity vehicleShipper) {
                if (PlanOptimizationStatus.valueOf(this.optimizationStatus)
                                .ordinal() >= PlanOptimizationStatus.OPTIMIZING.ordinal()) {
                        throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
                }

                this.vehicleShippers.remove(vehicleShipper);
                this.totalVehicles = this.vehicleShippers.size();
        }

}
