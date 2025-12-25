package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.AddressUpdateForm;
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
@Table(name = "wms2_address")
public class AddressEntity {

    @Id
    private String id;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "address_type")
    private String addressType;

    private float latitude;

    private float longitude;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "full_address")
    private String fullAddress;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    public AddressEntity(AddressCreationForm form, Long tenantId) {
        String addressId = IdUtils.generateAddressId();
        this.id = addressId;
        this.entityId = form.getEntityId();
        this.entityType = form.getEntityType();
        this.addressType = form.getAddressType();
        this.latitude = form.getLatitude();
        this.longitude = form.getLongitude();
        this.isDefault = form.isDefault();
        this.fullAddress = form.getFullAddress();
        this.tenantId = tenantId;
    }

    public void update(AddressUpdateForm form) {
        if (StringUtils.hasText(form.getAddressType()))
            this.addressType = form.getAddressType();

        this.latitude = form.getLatitude();
        this.longitude = form.getLongitude();

        this.isDefault = form.isDefault();

        if (StringUtils.hasText(form.getFullAddress()))
            this.fullAddress = form.getFullAddress();
    }

}
