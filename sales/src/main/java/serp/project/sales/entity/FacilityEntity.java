package serp.project.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.sales.constant.EntityType;
import serp.project.sales.dto.request.AddressCreationForm;
import serp.project.sales.dto.request.FacilityCreationForm;
import serp.project.sales.dto.request.FacilityUpdateForm;
import serp.project.sales.util.IdUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_facility")
public class FacilityEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "current_address_id")
    private String currentAddressId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "is_default")
    private boolean isDefault;

    private String phone;

    @Column(name = "postal_code")
    private String postalCode;

    private float length;
    private float width;
    private float height;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private AddressEntity address;

    public FacilityEntity(FacilityCreationForm form, Long tenantId) {
        this.id = IdUtils.generateFacilityId();
        this.name = form.getName();
        this.statusId = form.getStatusId();
        this.isDefault = true;
        this.phone = form.getPhone();
        this.postalCode = form.getPostalCode();
        this.length = form.getLength();
        this.width = form.getWidth();
        this.height = form.getHeight();
        this.tenantId = tenantId;

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(this.id);
        addressForm.setEntityType(EntityType.FACILITY.name());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        this.address = new AddressEntity(addressForm, tenantId);
    }

    public void update(FacilityUpdateForm form) {
        if (StringUtils.hasText(form.getName()))
            this.name = form.getName();

        if (StringUtils.hasText(form.getPhone()))
            this.phone = form.getPhone();

        if (StringUtils.hasText(form.getStatusId()))
            this.statusId = form.getStatusId();

        this.isDefault = form.isDefault();

        if (StringUtils.hasText(form.getPostalCode()))
            this.postalCode = form.getPostalCode();

        if (form.getLength() != 0)
            this.length = form.getLength();
        if (form.getWidth() != 0)
            this.width = form.getWidth();
        if (form.getHeight() != 0)
            this.height = form.getHeight();
    }

}
