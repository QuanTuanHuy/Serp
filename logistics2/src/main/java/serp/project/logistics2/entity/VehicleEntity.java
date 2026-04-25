package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.VehicleStatus;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_vehicle")
public class VehicleEntity {

    @Id
    private String id;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "max_weight_kg")
    private Long maxWeightKg;

    @Column(name = "max_volume_cbm")
    private Double maxVolumeCbm;

    private String status;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    public VehicleEntity(String id, String licensePlate, String vehicleType, Long maxWeightKg, Double maxVolumeCbm,
            String status, Long tenantId) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.maxWeightKg = maxWeightKg;
        this.maxVolumeCbm = maxVolumeCbm;
        this.status = status;
        this.tenantId = tenantId;
    }

    public static VehicleEntity create(
            String licensePlate,
            String vehicleType,
            Long maxWeightKg,
            Double maxVolumeCbm,
            Long tenantId) {
        String id = IdUtils.generateVehicleId();
        String status = VehicleStatus.IN_USE.name();
        return new VehicleEntity(id, licensePlate, vehicleType, maxWeightKg, maxVolumeCbm, status, tenantId);
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    public void update(String licensePlate, String vehicleType, Long maxWeightKg, Double maxVolumeCbm) {
        if (licensePlate != null) {
            this.licensePlate = licensePlate;
        }
        if (vehicleType != null) {
            this.vehicleType = vehicleType;
        }
        if (maxWeightKg != null) {
            this.maxWeightKg = maxWeightKg;
        }
        if (maxVolumeCbm != null) {
            this.maxVolumeCbm = maxVolumeCbm;
        }
    }

}
