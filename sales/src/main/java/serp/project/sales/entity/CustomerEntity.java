package serp.project.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.sales.dto.request.CustomerUpdateForm;
import serp.project.sales.constant.EntityType;
import serp.project.sales.dto.request.AddressCreationForm;
import serp.project.sales.dto.request.CustomerCreationForm;
import serp.project.sales.util.IdUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_customer")
public class CustomerEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "current_address_id")
    private String currentAddressId;

    @Column(name = "status_id")
    private String statusId;

    private String phone;

    private String email;

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

    public CustomerEntity(CustomerCreationForm form, Long tenantId) {
        String customerId = IdUtils.generateCustomerId();
        this.id = customerId;
        this.name = form.getName();
        this.email = form.getEmail();
        this.phone = form.getPhone();
        this.statusId = form.getStatusId();
        this.tenantId = tenantId;

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(customerId);
        addressForm.setEntityType(EntityType.SUPPLIER.name());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        this.address = new AddressEntity(addressForm, tenantId);
    }

    public void update(CustomerUpdateForm form) {
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
