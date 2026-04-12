package serp.project.logistics2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.DeliverySlipStatus;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_delivery_slip")
public class DeliverySlipEntity {

    @Id
    private String id;

    private String code;

    @Column(name = "outbound_shipment_id")
    private String outboundShipmentId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "facility_id")
    private String facilityId;

    private String status;

    @Column(name = "total_weight_kg")
    private Long totalWeightKg;

    @Column(name = "total_volume_cbm")
    private Double totalVolumeCbm;

    // Additional
    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_address_id")
    private String customerAddressId;

    @Column(name = "facility_address_id")
    private String facilityAddressId;

    // Essential
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

    // Transient
    @Transient
    private FacilityEntity facility;

    @Transient
    private AddressEntity customerAddress;

    @Transient
    private AddressEntity facilityAddress;

    @Transient
    private List<DeliveryItemEntity> items =  new ArrayList<>();

    public DeliverySlipEntity(
            String id,
            String code,
            String outboundShipmentId,
            String customerId,
            String facilityId,
            String status,
            Long totalWeightKg,
            Double totalVolumeCbm,
            String customerName,
            String customerPhone,
            String customerAddressId,
            String facilityAddressId,
            List<DeliveryItemEntity> items,
            Long createdByUserId,
            Long tenantId) {
        this.id = id;
        this.code = code;
        this.outboundShipmentId = outboundShipmentId;
        this.customerId = customerId;
        this.facilityId = facilityId;
        this.status = status;
        this.totalWeightKg = totalWeightKg;
        this.totalVolumeCbm = totalVolumeCbm;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddressId = customerAddressId;
        this.facilityAddressId = facilityAddressId;
        this.items = items;
        this.createdByUserId = createdByUserId;
        this.tenantId = tenantId;
    }

    public static DeliverySlipEntity create(
            String outboundShipmentId,
            CustomerEntity customer,
            FacilityEntity facility,
            Long createdByUserId,
            Long tenantId,
            List<DeliveryItemEntity> items) {
        String id = IdUtils.generateDeliverySlipId();
        String code = "DS-" + id.substring(6, 12); // Example: DS-XXXXXX
        String status = DeliverySlipStatus.PENDING.name();
        Long totalWeightKg = items.stream().mapToLong(item -> (long) item.getWeightKg()).sum();
        Double totalVolumeCbm = items.stream().mapToDouble(item -> item.getHeightM() * item.getWidthM() * item.getLengthM()).sum();
        items.forEach(item -> item.setDeliverySlipId(id));
        return new DeliverySlipEntity(
                id,
                code,
                outboundShipmentId,
                customer.getId(),
                facility.getId(),
                status,
                totalWeightKg,
                totalVolumeCbm,
                customer.getName(),
                customer.getPhone(),
                customer.getCurrentAddressId(),
                facility.getCurrentAddressId(),
                items,
                createdByUserId,
                tenantId);
    }

    public static DeliverySlipEntity createProxy(String id) {
        DeliverySlipEntity proxy = new DeliverySlipEntity();
        proxy.setId(id);
        return proxy;
    }

    public void addItem(DeliveryItemEntity item) {
        if (DeliverySlipStatus.valueOf(this.status).ordinal() > DeliverySlipStatus.PENDING.ordinal()) {
            throw new AppException(AppErrorCode.DELIVERY_SLIP_ALREADY_ASSIGNED);
        }

        item.setDeliverySlipId(id);
        this.items.add(item);
        this.totalWeightKg += (long) item.getWeightKg();
        double volume = item.getHeightM() * item.getWidthM() * item.getLengthM();
        this.totalVolumeCbm += (long) volume;
    }

    public void removeItem(DeliveryItemEntity item) {
        if (DeliverySlipStatus.valueOf(this.status).ordinal() > DeliverySlipStatus.PENDING.ordinal()) {
            throw new AppException(AppErrorCode.DELIVERY_SLIP_ALREADY_ASSIGNED);
        }

        this.totalWeightKg -= (long) item.getWeightKg();
        double volume = item.getHeightM() * item.getWidthM() * item.getLengthM();
        this.totalVolumeCbm -= (long) volume;
        this.items.remove(item);
    }

    public void updateQuantity(DeliveryItemEntity item, int newQuantity) {
        if (DeliverySlipStatus.valueOf(this.status).ordinal() > DeliverySlipStatus.PENDING.ordinal()) {
            throw new AppException(AppErrorCode.DELIVERY_SLIP_ALREADY_ASSIGNED);
        }

        int oldQuantity = item.getQuantity();
        item.update(newQuantity);
        int quantityDiff = newQuantity - oldQuantity;
        this.totalWeightKg += (long) (item.getWeightKg() * quantityDiff);
        double volumeDiff = (item.getHeightM() * item.getWidthM() * item.getLengthM()) * quantityDiff;
        this.totalVolumeCbm += (long) volumeDiff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeliverySlipEntity))
            return false;
        return id != null && id.equals(((DeliverySlipEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
