package serp.project.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import serp.project.sales.dto.request.InventoryItemCreationForm;
import serp.project.sales.dto.request.InventoryItemUpdateForm;
import serp.project.sales.util.IdUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_inventory_item")
public class InventoryItemEntity {

    @Id
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "quantity_on_hand")
    private int quantityOnHand;

    @Column(name = "quantity_reserved")
    private int quantityReserved;

    @Column(name = "quantity_committed")
    private int quantityCommitted;

    @Column(name = "facility_id")
    private String facilityId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "lot_id")
    private String lotId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "tenant_id")
    private Long tenantId;

    public InventoryItemEntity(InventoryItemCreationForm form, Long tenantId) {
        String inventoryItemId = IdUtils.generateInventoryItemId();
        this.id = inventoryItemId;
        this.productId = form.getProductId();
        this.quantityOnHand = form.getQuantity();
        this.quantityReserved = 0;
        this.quantityCommitted = 0;
        this.lotId = form.getLotId();
        this.facilityId = form.getFacilityId();
        this.expirationDate = form.getExpirationDate();
        this.manufacturingDate = form.getManufacturingDate();
        this.statusId = form.getStatusId();
        this.tenantId = tenantId;
    }

    public void update(InventoryItemUpdateForm form) {
        if (form.getQuantity() >= this.quantityReserved + this.quantityCommitted)
            this.quantityOnHand = form.getQuantity();

        if (form.getExpirationDate() != null)
            this.expirationDate = form.getExpirationDate();

        if (form.getManufacturingDate() != null)
            this.manufacturingDate = form.getManufacturingDate();

        if (form.getStatusId() != null)
            this.statusId = form.getStatusId();
    }

    public void reserveQuantity(int quantity) {
        this.quantityReserved += quantity;
    }

    public void releaseReservedQuantity(int quantity) {
        this.quantityReserved -= quantity;
    }

    public void commitQuantity(int quantity) {
        this.quantityReserved -= quantity;
        this.quantityCommitted += quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InventoryItemEntity inventoryItem = (InventoryItemEntity) o;
        return Objects.equals(id, inventoryItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
