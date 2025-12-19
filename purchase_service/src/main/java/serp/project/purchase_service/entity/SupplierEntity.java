package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.purchase_service.constant.EntityType;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.SupplierCreationForm;
import serp.project.purchase_service.dto.request.SupplierUpdateForm;
import serp.project.purchase_service.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "wms2_supplier")
public class SupplierEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "current_address_id")
    private String currentAddressId;

    private String email;

    private String phone;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "tenant_id")
    private Long tenantId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Transient
    private AddressEntity address;

    public SupplierEntity(SupplierCreationForm form, Long tenantId) {
        String supplierId = IdUtils.generateSupplierId();
        this.id = supplierId;
        this.name = form.getName();
        this.email = form.getEmail();
        this.phone = form.getPhone();
        this.statusId = form.getStatusId();
        this.tenantId = tenantId;

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(supplierId);
        addressForm.setEntityType(EntityType.SUPPLIER.name());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        this.address = new AddressEntity(addressForm, tenantId);
    }

    public void update(SupplierUpdateForm form) {
        if (StringUtils.hasText(form.getName()))
            this.name = form.getName();

        if (StringUtils.hasText(form.getEmail()))
            this.email = form.getEmail();

        if (StringUtils.hasText(form.getPhone()))
            this.phone = form.getPhone();

        if (StringUtils.hasText(form.getStatusId()))
            this.statusId = form.getStatusId();
    }

}
