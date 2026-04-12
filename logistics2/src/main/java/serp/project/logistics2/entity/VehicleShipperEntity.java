package serp.project.logistics2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.VehicleShipperStatus;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_vehicle_shipper")
public class VehicleShipperEntity {

    @Id
    private String id;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "shipper_id")
    private Long shipperId;

    @Column(name = "working_date")
    private LocalDate workingDate;

    private String status;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private VehicleEntity vehicle;

    public VehicleShipperEntity(String id, String vehicleId, Long shipperId, LocalDate workingDate, String status,
            Long tenantId) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.shipperId = shipperId;
        this.workingDate = workingDate;
        this.status = status;
        this.tenantId = tenantId;
    }

    public static VehicleShipperEntity create(String vehicleId, Long shipperId, LocalDate workingDate, Long tenantId) {
        String id = IdUtils.generateVehicleShipperId();
        String status = VehicleShipperStatus.ACTIVE.name();
        return new VehicleShipperEntity(id, vehicleId, shipperId, workingDate, status, tenantId);
    }

    public static VehicleShipperEntity createProxy(String id) {
        VehicleShipperEntity proxy = new VehicleShipperEntity();
        proxy.setId(id);
        return proxy;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VehicleShipperEntity))
            return false;
        return id != null && id.equals(((VehicleShipperEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
